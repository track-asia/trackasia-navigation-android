export BUILDTYPE ?= Debug
export RENDERER ?= drawable
export IS_LOCAL_DEVELOPMENT ?= true
export TARGET_BRANCH ?= main

# Đọc phiên bản từ file VERSION nếu tồn tại
VERSION_FILE := $(shell if [ -f VERSION ]; then cat VERSION; fi)
VERSION_NAME := $(VERSION_FILE)

CMAKE ?= cmake


ifeq ($(BUILDTYPE), Release)
else ifeq ($(BUILDTYPE), RelWithDebInfo)
else ifeq ($(BUILDTYPE), Sanitize)
else ifeq ($(BUILDTYPE), Debug)
else
  $(error BUILDTYPE must be Debug, Sanitize, Release or RelWithDebInfo)
endif

ifeq ($(RENDERER), drawable)
else ifeq ($(RENDERER), legacy)
else ifeq ($(RENDERER), vulkan)
else
  $(error RENDERER must be 'legacy' (OpenGL), 'drawable' (OpenGL) or 'vulkan')
endif

buildtype := $(shell echo "$(BUILDTYPE)" | tr "[A-Z]" "[a-z]")

ifeq ($(shell uname -s), Darwin)
  HOST_PLATFORM = macos
  HOST_PLATFORM_VERSION = $(shell uname -m)
  export NINJA = platform/macos/ninja
  export JOBS ?= $(shell sysctl -n hw.ncpu)
else ifeq ($(shell uname -s), Linux)
  HOST_PLATFORM = linux
  HOST_PLATFORM_VERSION = $(shell uname -m)
  export NINJA = platform/linux/ninja
  export JOBS ?= $(shell grep --count processor /proc/cpuinfo)
else
  $(error Cannot determine host platform)
endif

#### Android targets ###########################################################

MLN_ANDROID_ABIS  = arm-v7;armeabi-v7a
MLN_ANDROID_ABIS += arm-v8;arm64-v8a
MLN_ANDROID_ABIS += x86;x86
MLN_ANDROID_ABIS += x86-64;x86_64

MLN_ANDROID_LOCAL_WORK_DIR = /data/local/tmp/core-tests
MLN_ANDROID_LOCAL_BENCHMARK_DIR = /data/local/tmp/benchmark
MLN_ANDROID_LIBDIR = lib$(if $(filter arm-v8 x86-64,$1),64)
MLN_ANDROID_DALVIKVM = dalvikvm$(if $(filter arm-v8 x86-64,$1),64,32)
MLN_ANDROID_APK_SUFFIX = $(if $(filter Release,$(BUILDTYPE)),release,debug)
MLN_ANDROID_CORE_TEST_DIR = TrackAsiaAndroid/.externalNativeBuild/cmake/$(buildtype)/$2/core-tests
MLN_ANDROID_BENCHMARK_DIR = TrackAsiaAndroid/.externalNativeBuild/cmake/$(buildtype)/$2/benchmark
MLN_ANDROID_STL ?= c++_static
SOCKET_TIMEOUT = 360000
CONNECTION_TIMEOUT = 360000
MLN_ANDROID_GRADLE = ./gradlew --parallel --max-workers=$(JOBS) -Pmapbox.buildtype=$(buildtype) -Pmapbox.stl=$(MLN_ANDROID_STL) -Dorg.gradle.internal.http.socketTimeout=$(SOCKET_TIMEOUT) -Dorg.gradle.internal.http.connectionTimeout=$(CONNECTION_TIMEOUT)
MLN_ANDROID_GRADLE_SINGLE_JOB = ./gradlew --parallel --max-workers=1 -Pmapbox.buildtype=$(buildtype) -Pmapbox.stl=$(MLN_ANDROID_STL) -Dorg.gradle.internal.http.socketTimeout=$(SOCKET_TIMEOUT) -Dorg.gradle.internal.http.connectionTimeout=$(CONNECTION_TIMEOUT)

# Generate code based on the style specification
.PHONY: android-style-code
android-style-code:
	node scripts/generate-style-code.js
style-code: android-style-code

# Kiểm tra các lệnh sed trong Makefile và thay thế
ifeq ($(shell uname -s), Darwin)
	# Cho macOS, sử dụng phiên bản sed phù hợp
	SED_CMD = sed -E
else
	# Cho Linux và các hệ điều hành khác
	SED_CMD = sed
endif

# Tách các định nghĩa trong target dưới đây
define ANDROID_RULES
# $1 = arm-v7 (short arch)
# $2 = armeabi-v7a (internal arch)

.PHONY: android-test-lib-$1
android-test-lib-$1:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 -Pmapbox.with_test=true :TrackAsiaAndroidTestApp:assemble$(BUILDTYPE)

.PHONY: android-benchmark-$1
android-benchmark-$1:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 -Pmapbox.with_benchmark=true :TrackAsiaAndroidTestApp:assemble$(BUILDTYPE)

# Build SDK for for specified abi
.PHONY: android-lib-$1
android-lib-$1:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroid:assemble$(RENDERER)$(BUILDTYPE)

# Build test app and SDK for for specified abi
.PHONY: android-$1
android-$1:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:assemble$(BUILDTYPE)

# Build the core test for specified abi
.PHONY: android-core-test-$1
android-core-test-$1: android-test-lib-$1
	# Compile main sources and extract the classes (using the test app to get all transitive dependencies in one place)
	mkdir -p $(MLN_ANDROID_CORE_TEST_DIR)
	unzip -o TrackAsiaAndroidTestApp/build/outputs/apk/$(buildtype)/TrackAsiaAndroidTestApp-$(MLN_ANDROID_APK_SUFFIX).apk classes.dex -d $(MLN_ANDROID_CORE_TEST_DIR)

run-android-core-test-$1-%: android-core-test-$1
	# Ensure clean state on the device
	adb shell "rm -Rf $(MLN_ANDROID_LOCAL_WORK_DIR) && mkdir -p $(MLN_ANDROID_LOCAL_WORK_DIR)/test && mkdir -p $(MLN_ANDROID_LOCAL_WORK_DIR)/scripts/style-spec-reference"

	# Push all needed files to the device
	adb push $(MLN_ANDROID_CORE_TEST_DIR)/classes.dex $(MLN_ANDROID_LOCAL_WORK_DIR) > /dev/null 2>&1
	adb push TrackAsiaAndroid/build/intermediates/intermediate-jars/$(buildtype)/jni/$2/libtrackasia.so $(MLN_ANDROID_LOCAL_WORK_DIR) > /dev/null 2>&1
	adb push test/fixtures $(MLN_ANDROID_LOCAL_WORK_DIR)/test > /dev/null 2>&1
	adb push scripts/style-spec-reference/v8.json $(MLN_ANDROID_LOCAL_WORK_DIR)/scripts/style-spec-reference > /dev/null 2>&1
	adb push TrackAsiaAndroid/build/intermediates/cmake/$(buildtype)/obj/$2/mbgl-test $(MLN_ANDROID_LOCAL_WORK_DIR) > /dev/null 2>&1

# Create gtest filter for skipped tests.
	$(eval SKIPPED_TESTS := -$(shell grep -v "^#" tests/skipped.txt | grep -v "^$$" | paste -sd ":" -))

	# Kick off the tests
	adb shell "export LD_LIBRARY_PATH=$(MLN_ANDROID_LOCAL_WORK_DIR) && cd $(MLN_ANDROID_LOCAL_WORK_DIR) && chmod +x mbgl-test && ./mbgl-test --class_path=$(MLN_ANDROID_LOCAL_WORK_DIR)/classes.dex --gtest_filter=$$*:$(SKIPPED_TESTS)"

	# Gather the results and unpack them
	adb shell "cd $(MLN_ANDROID_LOCAL_WORK_DIR) && tar -cvzf results.tgz test/fixtures/*  > /dev/null 2>&1"
	adb pull $(MLN_ANDROID_LOCAL_WORK_DIR)/results.tgz $(MLN_ANDROID_CORE_TEST_DIR)/ > /dev/null 2>&1
	rm -rf $(MLN_ANDROID_CORE_TEST_DIR)/results && mkdir -p $(MLN_ANDROID_CORE_TEST_DIR)/results
	tar -xzf $(MLN_ANDROID_CORE_TEST_DIR)/results.tgz --strip-components=2 -C $(MLN_ANDROID_CORE_TEST_DIR)/results

# Run the core test for specified abi
.PHONY: run-android-core-test-$1
run-android-core-test-$1: run-android-core-test-$1-*

# Run benchmarks for specified abi
.PHONY: run-android-benchmark-$1
run-android-benchmark-$1: run-android-benchmark-$1-*

run-android-benchmark-$1-%: android-benchmark-$1
	mkdir -p $(MLN_ANDROID_BENCHMARK_DIR)
	unzip -o TrackAsiaAndroidTestApp/build/outputs/apk/$(buildtype)/TrackAsiaAndroidTestApp-$(MLN_ANDROID_APK_SUFFIX).apk classes.dex -d $(MLN_ANDROID_BENCHMARK_DIR)

	# Delete old test folder and create new one
	adb shell "rm -Rf $(MLN_ANDROID_LOCAL_BENCHMARK_DIR) && mkdir -p $(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/benchmark && mkdir -p $(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/test"

	# Push compiled java sources, test data and executable to device
	adb push $(MLN_ANDROID_BENCHMARK_DIR)/classes.dex $(MLN_ANDROID_LOCAL_BENCHMARK_DIR) > /dev/null 2>&1
	adb push TrackAsiaAndroid/build/intermediates/intermediate-jars/$(buildtype)/jni/$2/libtrackasia.so $(MLN_ANDROID_LOCAL_BENCHMARK_DIR) > /dev/null 2>&1
	adb push benchmark/fixtures $(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/benchmark > /dev/null 2>&1
	adb push test/fixtures $(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/test > /dev/null 2>&1
	adb push TrackAsiaAndroid/build/intermediates/cmake/$(buildtype)/obj/$2/mbgl-benchmark $(MLN_ANDROID_LOCAL_BENCHMARK_DIR) > /dev/null 2>&1

	# Run benchmark. Number of benchmark iterations can be set by run-android-benchmark-N parameter.
	adb shell "export LD_LIBRARY_PATH=$(MLN_ANDROID_LOCAL_BENCHMARK_DIR) && cd $(MLN_ANDROID_LOCAL_BENCHMARK_DIR) && chmod +x mbgl-benchmark && ./mbgl-benchmark --class_path=$(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/classes.dex --benchmark_repetitions=$$* --benchmark_format=json --benchmark_out=results.json"

	# Pull results.json from the device
	rm -rf $(MLN_ANDROID_BENCHMARK_DIR)/results && mkdir -p $(MLN_ANDROID_BENCHMARK_DIR)/results
	adb pull $(MLN_ANDROID_LOCAL_BENCHMARK_DIR)/results.json $(MLN_ANDROID_BENCHMARK_DIR)/results > /dev/null 2>&1

# Run the test app on connected android device with specified abi
.PHONY: run-android-$1
run-android-$1:
	-adb uninstall com.trackasia.testapp 2> /dev/null
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:install$(BUILDTYPE) && adb shell am start -n com.trackasia.testapp/.activity.FeatureOverviewActivity

# Build test app instrumentation tests apk and test app apk for specified abi
.PHONY: android-ui-test-$1
android-ui-test-$1:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:assembleDebug :TrackAsiaAndroidTestApp:assembleAndroidTest

# Run test app instrumentation tests on a connected android device or emulator with specified abi
.PHONY: run-android-ui-test-$1
run-android-ui-test-$1:
	-adb uninstall com.trackasia.testapp 2> /dev/null
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:connectedAndroidTest

# Run Java Instrumentation tests on a connected android device or emulator with specified abi and test filter
run-android-ui-test-$1-%:
	-adb uninstall com.trackasia.testapp 2> /dev/null
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$$*"

# Symbolicate native stack trace with the specified abi
.PHONY: android-ndk-stack-$1
android-ndk-stack-$1:
	adb logcat | ndk-stack -sym TrackAsiaAndroid/build/intermediates/cmake/debug/obj/$2/

# Run render tests with pixelmatch
.PHONY: run-android-render-test-$1
run-android-render-test-$1: $(BUILD_DEPS)
	-adb uninstall com.trackasia.testapp 2> /dev/null
	# delete old test results
	rm -rf build/render-test/trackasia/
  # copy test definitions & ignore file to test app assets folder, clear old ones first
	rm -rf TrackAsiaAndroidTestApp/src/main/assets/integration
	cp -r metrics/integration TrackAsiaAndroidTestApp/src/main/assets
	cp platform/node/test/ignores.json TrackAsiaAndroidTestApp/src/main/assets/integration/ignores.json
	# run RenderTest.java to generate static map images
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=$2 :TrackAsiaAndroidTestApp:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="com.trackasia.testapp.render.RenderTest"
	# pull generated images from the device
	adb pull "`adb shell 'printenv EXTERNAL_STORAGE' | tr -d '\r'`/trackasia/render" build/render-test
	# copy expected result and run pixelmatch
	python scripts/run-render-test.py
	# remove test definitions from assets
	rm -rf TrackAsiaAndroidTestApp/src/main/assets/integration

endef

# Explodes the arguments into individual variables
define ANDROID_RULES_INVOKER
$(call ANDROID_RULES,$(word 1,$1),$(word 2,$1))
endef

$(foreach abi,$(MLN_ANDROID_ABIS),$(eval $(call ANDROID_RULES_INVOKER,$(subst ;, ,$(abi)))))

# Build the Android SDK and test app with abi set to arm-v7
.PHONY: android
android: android-arm-v7

# Build the Android SDK with abi set to arm-v7
.PHONY: android-lib
android-lib: android-lib-arm-v7

# Run the test app on connected android device with abi set to arm-v7
.PHONY: run-android
run-android: run-android-arm-v7

# Run Java Instrumentation tests on a connected android device or emulator with abi set to arm-v7
.PHONY: run-android-ui-test
run-android-ui-test: run-android-ui-test-arm-v7
run-android-ui-test-%: run-android-ui-test-arm-v7-%

# Run Java Unit tests on the JVM of the development machine executing this
.PHONY: run-android-unit-test
run-android-unit-test:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=none :TrackAsiaAndroid:testLegacyDebugUnitTest --info
run-android-unit-test-%:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=none :TrackAsiaAndroid:testLegacyDebugUnitTest --info --tests "$*"

DEBUG_TAR_FILE_NAME := $(if $(findstring drawable,$(RENDERER)),debug-symbols-opengl.tar.gz,debug-symbols-$(RENDERER).tar.gz)

# Builds a release package and .tar.gz with debug symbols of the Android SDK
.PHONY: apackage
apackage:
	echo "Building for $(RENDERER)"
	make android-lib-arm-v7 && make android-lib-arm-v8 && make android-lib-x86 && make android-lib-x86-64
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=all assemble$(RENDERER)$(BUILDTYPE)
	mkdir -p build
	tar -czvf build/$(DEBUG_TAR_FILE_NAME) -C TrackAsiaAndroid/build/intermediates/library_jni/$(RENDERER)Release/*JniLibsProjectOnly/jni .

# Build test app instrumentation tests apk and test app apk for all abi's
.PHONY: android-ui-test
android-ui-test:
	$(MLN_ANDROID_GRADLE) -Ptrackasia.abis=all :TrackAsiaAndroidTestApp:assembleDebug :TrackAsiaAndroidTestApp:assembleAndroidTest

#Run instrumentations tests on MicroSoft App Center, ${devices} can be "xiaomi","huawei","OnePlus","htc"
.PHONY: run-android-test-app-center
run-android-test-app-center:
	appcenter test run espresso --app "TrackAsia-mobile/Maps-sdk" --devices "TrackAsia-mobile/${devices}" --app-path TrackAsiaAndroidTestApp/build/outputs/apk/debug/TrackAsiaAndroidTestApp-debug.apk  --test-series "master" --locale "en_US" --build-dir TrackAsiaAndroidTestApp/build/outputs/apk/androidTest/debug --token ${APPCENTER_ACCESS_TOKEN}


# Ký tất cả các artifact đã tạo ra
.PHONY: sign-all-artifacts
sign-all-artifacts:
	@echo "🔏 Ký tất cả các artifact..."
	@if [ -z "$(GPG_KEY_ID)" ]; then \
		echo "❌ Vui lòng cung cấp ID khóa GPG bằng cách thêm GPG_KEY_ID=<id_khóa>"; \
		exit 1; \
	fi
	
	@echo "📋 Kiểm tra khóa GPG $(GPG_KEY_ID)..."
	@if ! gpg --list-keys $(GPG_KEY_ID) > /dev/null 2>&1; then \
		echo "❌ Không tìm thấy khóa GPG $(GPG_KEY_ID) trên hệ thống"; \
		exit 1; \
	fi
	
	@echo "🔍 Tìm các file để ký..."
	@find ~/.m2/repository/io/github/track-asia -type f > .temp_files.txt
	@grep -v "\.md5$$" .temp_files.txt | grep -v "\.sha1$$" | grep -v "\.asc$$" | grep -v "maven-metadata" > .temp_files_to_sign.txt
	@TOTAL_FILES=$$(cat .temp_files_to_sign.txt | wc -l | tr -d ' \n\t')
	
	@if [ "$$TOTAL_FILES" = "0" ]; then \
		echo "❌ Không tìm thấy file nào để ký. Vui lòng chạy 'make run-android-local-publish' trước."; \
		rm -f .temp_files.txt .temp_files_to_sign.txt; \
		exit 1; \
	fi
	
	@echo "Đã tìm thấy $$TOTAL_FILES file cần ký. Bắt đầu quá trình ký..."
	
	@COUNT=0
	@cat .temp_files_to_sign.txt | while read file; do \
		COUNT=$$((COUNT+1)); \
		echo "[$${COUNT}/$$TOTAL_FILES] Đang ký: $$(basename $$file)"; \
		\
		md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
		chmod 644 "$$file.md5"; \
		\
		sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
		chmod 644 "$$file.sha1"; \
		\
		gpg --batch --yes --passphrase="track-asia" --use-agent --local-user $(GPG_KEY_ID) --armor --detach-sign "$$file"; \
		\
		chmod 644 "$$file.asc"; \
	done
	
	@echo ""
	@echo "📊 Tổng kết:"
	@echo "  • Đã ký $$TOTAL_FILES file."
	@echo "  • Quá trình ký hoàn tất."
	
	@rm -f .temp_files.txt .temp_files_to_sign.txt
	@echo "✅ Hoàn tất quá trình ký."

# Mục tiêu mới để ký các file iOS
.PHONY: sign-ios-files
sign-ios-files:
	@echo "🔏 Ký các file iOS..."
	@if [ -z "$(GPG_KEY_ID)" ]; then \
		echo "❌ Vui lòng cung cấp ID khóa GPG bằng cách thêm GPG_KEY_ID=<id_khóa>"; \
		exit 1; \
	fi
	
	@MAVEN_REPO=~/.m2/repository/io/github/track-asia
	@VERSION="$(VERSION_NAME)"
	@echo "   Phiên bản: $$VERSION"
	
	@echo "📋 Kiểm tra khóa GPG $(GPG_KEY_ID)..."
	@if ! gpg --list-keys $(GPG_KEY_ID) > /dev/null 2>&1; then \
		echo "❌ Không tìm thấy khóa GPG $(GPG_KEY_ID) trên hệ thống"; \
		exit 1; \
	fi
	
	@echo ""
	@echo "📋 Ký các file iOS:"
	
	@echo "\n   🔏 Ký các file iosx64..."
	@FILES=( \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-metadata.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-sources.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.klib" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.module" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.pom" \
	)
	
	@COUNT_X64=0
	@TOTAL_X64=0
	
	@for file in "$${FILES[@]}"; do \
		TOTAL_X64=$$((TOTAL_X64+1)); \
		if [ -f "$$file" ]; then \
			echo "   Ký file: $$(basename $$file)"; \
			\
			md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
			chmod 644 "$$file.md5"; \
			\
			sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
			chmod 644 "$$file.sha1"; \
			\
			gpg --batch --yes --passphrase="track-asia" --use-agent --local-user $(GPG_KEY_ID) --armor --detach-sign "$$file"; \
			\
			chmod 644 "$$file.asc"; \
			COUNT_X64=$$((COUNT_X64+1)); \
		else \
			echo "   ⚠️ File không tồn tại: $$(basename $$file)"; \
		fi; \
	done
	
	@echo "\n   🔏 Ký các file iosarm64..."
	@FILES=( \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-metadata.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-sources.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.klib" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.module" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.pom" \
	)
	
	@COUNT_ARM64=0
	@TOTAL_ARM64=0
	
	@for file in "$${FILES[@]}"; do \
		TOTAL_ARM64=$$((TOTAL_ARM64+1)); \
		if [ -f "$$file" ]; then \
			echo "   Ký file: $$(basename $$file)"; \
			\
			md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
			chmod 644 "$$file.md5"; \
			\
			sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
			chmod 644 "$$file.sha1"; \
			\
			gpg --batch --yes --passphrase="track-asia" --use-agent --local-user $(GPG_KEY_ID) --armor --detach-sign "$$file"; \
			\
			chmod 644 "$$file.asc"; \
			COUNT_ARM64=$$((COUNT_ARM64+1)); \
		else \
			echo "   ⚠️ File không tồn tại: $$(basename $$file)"; \
		fi; \
	done
	
	@TOTAL=$$(( TOTAL_X64 + TOTAL_ARM64 ))
	@COUNT=$$(( COUNT_X64 + COUNT_ARM64 ))
	
	@echo ""
	@echo "📊 Tổng kết:"
	@echo "  • Tổng số file đã xử lý: $$TOTAL"
	@echo "  • Số file đã ký: $$COUNT"
	
	@if [ $$COUNT -eq 0 ]; then \
		echo "❌ Không có file iOS nào được ký. Vui lòng chạy 'make run-android-local-publish' trước khi ký."; \
		exit 1; \
	else \
		echo "✅ Đã ký $$COUNT file iOS thành công."; \
		if [ $$COUNT -lt $$TOTAL ]; then \
			echo "⚠️ Cảnh báo: Có $$(( TOTAL - COUNT )) file không tồn tại."; \
		fi; \
	fi

# Kiểm tra thiếu chữ ký và tự động ký nếu phát hiện
.PHONY: check-and-sign
check-and-sign:
	@echo "⚠️ DEPRECATED: Hàm này sẽ bị loại bỏ. Hãy sử dụng 'sign-all-artifacts GPG_KEY_ID=YourKeyID' thay thế."
	@echo "🔍 Kiểm tra các file bị thiếu chữ ký..."
	@VERSION=$(shell echo $(VERSION_NAME) | cut -d '=' -f 2); \
	MAVEN_REPO=~/.m2/repository/io/github/track-asia; \
	GPG_KEY_ID="795690AE"; \
	\
	find $$MAVEN_REPO -type f -not -path "*/\.*" | grep -v "\.\(md5\|sha1\|asc\)$$" | while read file; do \
		if [ ! -f "$$file.asc" ] || [ ! -f "$$file.md5" ] || [ ! -f "$$file.sha1" ]; then \
			echo "Phát hiện file thiếu chữ ký: $$file"; \
			\
			# Tạo MD5 nếu thiếu \
			if [ ! -f "$$file.md5" ]; then \
				md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
				echo "  ✓ Đã tạo MD5"; \
			fi; \
			\
			# Tạo SHA1 nếu thiếu \
			if [ ! -f "$$file.sha1" ]; then \
				sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
				echo "  ✓ Đã tạo SHA1"; \
			fi; \
			\
			# Tạo GPG signature nếu thiếu \
			if [ ! -f "$$file.asc" ]; then \
				gpg --batch --yes --armor --detach-sign --local-user "$$GPG_KEY_ID" "$$file"; \
				echo "  ✓ Đã tạo GPG signature"; \
			fi; \
		fi; \
	done
	@echo "✅ Đã kiểm tra và ký tất cả các file thiếu chữ ký."

# Mục tiêu mới để kiểm tra xác minh chữ ký đã được ký với đúng khóa
.PHONY: verify-ios-signatures
verify-ios-signatures:
	@echo "🔍 Kiểm tra chữ ký của các file iOS..."
	@MAVEN_REPO=~/.m2/repository/io/github/track-asia; \
	GPG_KEY_ID="795690AE"; \
	VERSION=$(shell echo $(VERSION_NAME) | cut -d '=' -f 2); \
	echo "   Phiên bản: $$VERSION"
	
	@echo "📋 Kiểm tra khóa GPG $${GPG_KEY_ID}..."
	@if ! gpg --list-keys $${GPG_KEY_ID} > /dev/null 2>&1; then \
		echo "❌ Không tìm thấy khóa GPG $${GPG_KEY_ID} trên hệ thống"; \
		exit 1; \
	fi
	
	@echo ""
	@echo "📋 Kiểm tra các file iOS:"
	
	@echo "\n   🔍 Xử lý các file iosx64..."
	@FILES=( \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-metadata.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-sources.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.klib" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.module" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.pom" \
	)
	
	@TOTAL_X64=0
	@VALID_X64=0
	@MISSING_X64=0
	@INVALID_X64=0
	
	@for file in "$${FILES[@]}"; do \
		TOTAL_X64=$$((TOTAL_X64+1)); \
		ASC_FILE="$${file}.asc"; \
		\
		if [ ! -f "$$file" ]; then \
			echo "   ❌ File gốc không tồn tại: $$(basename $$file)"; \
			MISSING_X64=$$((MISSING_X64+1)); \
			continue; \
		fi; \
		\
		if [ ! -f "$$ASC_FILE" ]; then \
			echo "   ❌ Không tìm thấy file chữ ký: $$(basename $$ASC_FILE)"; \
			MISSING_X64=$$((MISSING_X64+1)); \
			continue; \
		fi; \
		\
		echo "   Kiểm tra chữ ký: $$(basename $$file)"; \
		VERIFY_OUTPUT=$$(gpg --verify "$$ASC_FILE" "$$file" 2>&1); \
		\
		if echo "$$VERIFY_OUTPUT" | grep -q "Good signature"; then \
			VALID_X64=$$((VALID_X64+1)); \
			if echo "$$VERIFY_OUTPUT" | grep -q "$${GPG_KEY_ID}"; then \
				echo "   ✅ Chữ ký hợp lệ và được ký với khóa $${GPG_KEY_ID}"; \
			else \
				KEY_USED=$$(echo "$$VERIFY_OUTPUT" | grep -o "key ID [A-Z0-9]*" | awk '{print $$3}'); \
				echo "   ⚠️ Chữ ký hợp lệ nhưng được ký với khóa KHÁC ($${KEY_USED})"; \
			fi; \
		else \
			echo "   ❌ Chữ ký KHÔNG hợp lệ"; \
			INVALID_X64=$$((INVALID_X64+1)); \
		fi; \
	done
	
	@echo "\n   🔍 Xử lý các file iosarm64..."
	@FILES=( \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-metadata.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION-sources.jar" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.klib" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.module" \
		"$$MAVEN_REPO/navigation-core-iosarm64/$$VERSION/navigation-core-iosarm64-$$VERSION.pom" \
	)
	
	@TOTAL_ARM64=0
	@VALID_ARM64=0
	@MISSING_ARM64=0
	@INVALID_ARM64=0
	
	@for file in "$${FILES[@]}"; do \
		TOTAL_ARM64=$$((TOTAL_ARM64+1)); \
		ASC_FILE="$${file}.asc"; \
		\
		if [ ! -f "$$file" ]; then \
			echo "   ❌ File gốc không tồn tại: $$(basename $$file)"; \
			MISSING_ARM64=$$((MISSING_ARM64+1)); \
			continue; \
		fi; \
		\
		if [ ! -f "$$ASC_FILE" ]; then \
			echo "   ❌ Không tìm thấy file chữ ký: $$(basename $$ASC_FILE)"; \
			MISSING_ARM64=$$((MISSING_ARM64+1)); \
			continue; \
		fi; \
		\
		echo "   Kiểm tra chữ ký: $$(basename $$file)"; \
		VERIFY_OUTPUT=$$(gpg --verify "$$ASC_FILE" "$$file" 2>&1); \
		\
		if echo "$$VERIFY_OUTPUT" | grep -q "Good signature"; then \
			VALID_ARM64=$$((VALID_ARM64+1)); \
			if echo "$$VERIFY_OUTPUT" | grep -q "$${GPG_KEY_ID}"; then \
				echo "   ✅ Chữ ký hợp lệ và được ký với khóa $${GPG_KEY_ID}"; \
			else \
				KEY_USED=$$(echo "$$VERIFY_OUTPUT" | grep -o "key ID [A-Z0-9]*" | awk '{print $$3}'); \
				echo "   ⚠️ Chữ ký hợp lệ nhưng được ký với khóa KHÁC ($${KEY_USED})"; \
			fi; \
		else \
			echo "   ❌ Chữ ký KHÔNG hợp lệ"; \
			INVALID_ARM64=$$((INVALID_ARM64+1)); \
		fi; \
	done
	
	@TOTAL=$$((TOTAL_X64 + TOTAL_ARM64))
	@VALID=$$((VALID_X64 + VALID_ARM64))
	@MISSING=$$((MISSING_X64 + MISSING_ARM64))
	@INVALID=$$((INVALID_X64 + INVALID_ARM64))
	
	@echo ""
	@echo "📊 Tổng kết kiểm tra:"
	@echo "  • Tổng số file: $$TOTAL"
	@echo "  • Chữ ký hợp lệ: $$VALID"
	@echo "  • Thiếu file hoặc chữ ký: $$MISSING"
	@echo "  • Chữ ký không hợp lệ: $$INVALID"
	
	@# Kiểm tra trên Maven Central
	@if curl -s -I "https://repo1.maven.org/maven2/io/github/track-asia/navigation-core-iosx64/$${VERSION}/" >/dev/null 2>&1; then \
		echo "\n🌐 iOS artifacts đã được xuất bản lên Maven Central với phiên bản $$VERSION"; \
	else \
		echo "\n🌐 iOS artifacts chưa được xuất bản lên Maven Central với phiên bản $$VERSION"; \
	fi

# Thêm mục tiêu để ký các file iOS với sudo để giải quyết vấn đề quyền
.PHONY: sudo-sign-ios-files
sudo-sign-ios-files:
	@echo "🔐 Đang ký các file iOS với quyền sudo..."
	@VERSION="2.0.1"; \
	MAVEN_REPO=~/.m2/repository/io/github/track-asia; \
	GPG_KEY_ID="795690AE"; \
	\
	echo "⚠️ Đang thay đổi quyền truy cập..."; \
	sudo chmod -R 755 $$MAVEN_REPO/navigation-core-iosx64/$$VERSION/; \
	sudo find $$MAVEN_REPO/navigation-core-iosx64/$$VERSION/ -type f -exec chmod 644 {} \;; \
	\
	FILES=( \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-metadata.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION-sources.jar" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.klib" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.module" \
		"$$MAVEN_REPO/navigation-core-iosx64/$$VERSION/navigation-core-iosx64-$$VERSION.pom" \
	); \
	\
	for file in "$${FILES[@]}"; do \
		if [ -f "$$file" ]; then \
			echo "Đang ký: $$file"; \
			\
			# Tạo MD5 \
			md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
			\
			# Tạo SHA1 \
			sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
			\
			# Xóa chữ ký cũ nếu có \
			rm -f "$$file.asc" 2>/dev/null; \
			\
			# Tạo GPG signature với sudo \
			gpg --batch --yes --armor --detach-sign --local-user "$$GPG_KEY_ID" "$$file"; \
			\
			# Kiểm tra kết quả \
			if [ -f "$$file.asc" ]; then \
				echo "✅ Đã ký thành công: $$file"; \
				\
				# Xác minh chữ ký \
				if gpg --verify "$$file.asc" "$$file" 2>/dev/null; then \
					echo "  ✓ Chữ ký hợp lệ"; \
				else \
					echo "  ❌ Chữ ký không hợp lệ"; \
				fi; \
			else \
				echo "❌ Không thể ký: $$file"; \
			fi; \
		else \
			echo "❌ Không tìm thấy file: $$file"; \
		fi; \
	done
	@echo "✅ Hoàn tất quá trình ký các file iOS với sudo."

# Kiểm tra tất cả các chữ ký đã tạo
.PHONY: verify-signatures
verify-signatures:
	@echo "🔍 Kiểm tra các chữ ký đã tạo..."
	@MAVEN_REPO=~/.m2/repository/io/github/track-asia; \
	\
	# Tìm tất cả các file MD5 \
	MD5_FILES=$$(find $$MAVEN_REPO -name "*.md5"); \
	MD5_COUNT=$$(echo "$$MD5_FILES" | grep -c "." || echo 0); \
	\
	# Tìm tất cả các file SHA1 \
	SHA1_FILES=$$(find $$MAVEN_REPO -name "*.sha1"); \
	SHA1_COUNT=$$(echo "$$SHA1_FILES" | grep -c "." || echo 0); \
	\
	# Tìm tất cả các file GPG \
	GPG_FILES=$$(find $$MAVEN_REPO -name "*.asc"); \
	GPG_COUNT=$$(echo "$$GPG_FILES" | grep -c "." || echo 0); \
	\
	# Kiểm tra các file gốc \
	ORIG_FILES=$$(find $$MAVEN_REPO -type f \
		-not -name "*.md5" \
		-not -name "*.sha1" \
		-not -name "*.asc" \
		-not -name "*.sha256" \
		-not -name "*.sha512" \
		-not -name "_remote.repositories" \
		-not -path "*/maven-metadata.xml*" | grep -v -e '\.lastUpdated$$' -e '\.repositories$$'); \
	ORIG_COUNT=$$(echo "$$ORIG_FILES" | grep -c "." || echo 0); \
	\
	# Kiểm tra số lượng chữ ký \
	echo "📊 Tổng kết file:"; \
	echo "  • File gốc cần ký: $$ORIG_COUNT"; \
	echo "  • File MD5: $$MD5_COUNT"; \
	echo "  • File SHA1: $$SHA1_COUNT"; \
	echo "  • File GPG: $$GPG_COUNT"; \
	\
	if [ $$ORIG_COUNT -eq 0 ]; then \
		echo "❌ Không tìm thấy file nào để kiểm tra. Hãy chạy 'make run-android-local-publish' trước."; \
		exit 1; \
	fi; \
	\
	echo ""; \
	echo "🧪 Kiểm tra tính toàn vẹn chữ ký MD5..."; \
	\
	MD5_VALID=0; \
	MD5_INVALID=0; \
	MD5_MISSING=0; \
	\
	for file in $$ORIG_FILES; do \
		if [ ! -f "$$file.md5" ]; then \
			echo "  ❌ Thiếu file MD5: $$(basename $$file)"; \
			MD5_MISSING=$$((MD5_MISSING+1)); \
			continue; \
		fi; \
		\
		EXPECTED=$$(cat "$$file.md5"); \
		ACTUAL=$$(md5sum "$$file" | cut -d ' ' -f 1); \
		\
		if [ "$$EXPECTED" = "$$ACTUAL" ]; then \
			MD5_VALID=$$((MD5_VALID+1)); \
		else \
			echo "  ❌ MD5 không khớp: $$(basename $$file)"; \
			echo "     - Mong đợi: $$EXPECTED"; \
			echo "     - Thực tế:  $$ACTUAL"; \
			MD5_INVALID=$$((MD5_INVALID+1)); \
		fi; \
	done; \
	\
	echo "  • MD5 hợp lệ: $$MD5_VALID"; \
	echo "  • MD5 không hợp lệ: $$MD5_INVALID"; \
	echo "  • Thiếu MD5: $$MD5_MISSING"; \
	\
	echo ""; \
	echo "🧪 Kiểm tra tính toàn vẹn chữ ký SHA1..."; \
	\
	SHA1_VALID=0; \
	SHA1_INVALID=0; \
	SHA1_MISSING=0; \
	\
	for file in $$ORIG_FILES; do \
		if [ ! -f "$$file.sha1" ]; then \
			echo "  ❌ Thiếu file SHA1: $$(basename $$file)"; \
			SHA1_MISSING=$$((SHA1_MISSING+1)); \
			continue; \
		fi; \
		\
		EXPECTED=$$(cat "$$file.sha1"); \
		ACTUAL=$$(sha1sum "$$file" | cut -d ' ' -f 1); \
		\
		if [ "$$EXPECTED" = "$$ACTUAL" ]; then \
			SHA1_VALID=$$((SHA1_VALID+1)); \
		else \
			echo "  ❌ SHA1 không khớp: $$(basename $$file)"; \
			echo "     - Mong đợi: $$EXPECTED"; \
			echo "     - Thực tế:  $$ACTUAL"; \
			SHA1_INVALID=$$((SHA1_INVALID+1)); \
		fi; \
	done; \
	\
	echo "  • SHA1 hợp lệ: $$SHA1_VALID"; \
	echo "  • SHA1 không hợp lệ: $$SHA1_INVALID"; \
	echo "  • Thiếu SHA1: $$SHA1_MISSING"; \
	\
	echo ""; \
	echo "🧪 Kiểm tra tính toàn vẹn chữ ký GPG..."; \
	\
	GPG_VALID=0; \
	GPG_INVALID=0; \
	GPG_MISSING=0; \
	\
	for file in $$ORIG_FILES; do \
		if [ ! -f "$$file.asc" ]; then \
			echo "  ❌ Thiếu file GPG: $$(basename $$file)"; \
			GPG_MISSING=$$((GPG_MISSING+1)); \
			continue; \
		fi; \
		\
		VERIFY_RESULT=$$(gpg --verify "$$file.asc" "$$file" 2>&1 || echo "Verification failed"); \
		\
		if echo "$$VERIFY_RESULT" | grep -q "Good signature"; then \
			GPG_VALID=$$((GPG_VALID+1)); \
		else \
			echo "  ❌ GPG không hợp lệ: $$(basename $$file)"; \
			GPG_INVALID=$$((GPG_INVALID+1)); \
		fi; \
	done; \
	\
	echo "  • GPG hợp lệ: $$GPG_VALID"; \
	echo "  • GPG không hợp lệ: $$GPG_INVALID"; \
	echo "  • Thiếu GPG: $$GPG_MISSING"; \
	\
	echo ""; \
	echo "📊 Tổng kết kiểm tra:"; \
	echo "  • Tổng số file gốc: $$ORIG_COUNT"; \
	echo "  • MD5 hợp lệ: $$MD5_VALID / $$ORIG_COUNT"; \
	echo "  • SHA1 hợp lệ: $$SHA1_VALID / $$ORIG_COUNT"; \
	echo "  • GPG hợp lệ: $$GPG_VALID / $$ORIG_COUNT"; \
	\
	# Kiểm tra tổng thể \
	if [ $$MD5_VALID -eq $$ORIG_COUNT ] && [ $$SHA1_VALID -eq $$ORIG_COUNT ] && [ $$GPG_VALID -eq $$ORIG_COUNT ]; then \
		echo "✅ Tất cả các chữ ký đều hợp lệ."; \
	else \
		echo "⚠️ Có một số chữ ký không hợp lệ hoặc bị thiếu:"; \
		if [ $$MD5_MISSING -gt 0 ] || [ $$MD5_INVALID -gt 0 ]; then \
			echo "  • MD5: $$MD5_MISSING thiếu, $$MD5_INVALID không hợp lệ"; \
		fi; \
		if [ $$SHA1_MISSING -gt 0 ] || [ $$SHA1_INVALID -gt 0 ]; then \
			echo "  • SHA1: $$SHA1_MISSING thiếu, $$SHA1_INVALID không hợp lệ"; \
		fi; \
		if [ $$GPG_MISSING -gt 0 ] || [ $$GPG_INVALID -gt 0 ]; then \
			echo "  • GPG: $$GPG_MISSING thiếu, $$GPG_INVALID không hợp lệ"; \
		fi; \
		\
		echo ""; \
		echo "💡 Gợi ý: Chạy 'make sign-all-artifacts GPG_KEY_ID=<your_gpg_key_id>' để ký lại tất cả các file."; \
	fi

# Show current version from VERSION file
.PHONY: show-version
show-version:
	@echo "Current version: $(VERSION_NAME)"

# Set a new version in the VERSION file
.PHONY: set-version
set-version:
	@if [ -z "$(NEW_VERSION)" ]; then \
		echo "❌ Error: NEW_VERSION is required"; \
		echo "Usage: make set-version NEW_VERSION=x.y.z"; \
		exit 1; \
	fi; \
	\
	echo "🔄 Setting version to $(NEW_VERSION)..."; \
	echo "$(NEW_VERSION)" > VERSION; \
	echo "✅ Version updated to $(NEW_VERSION)";

# Quy trình xuất bản trọn gói
.PHONY: publish-all
publish-all:
	@echo "🚀 Executing full publishing workflow..."
	
	@$(MAKE) run-android-local-publish
	
	@echo "📝 Verifying signatures..."
	@$(MAKE) verify-signatures
	
	@echo "📝 Verifying iOS signatures..."
	@$(MAKE) verify-ios-signatures
	
	@echo "📝 Verifying navigation-ui-android..."
	@$(MAKE) sign-navigation-ui-android GPG_KEY_ID=795690AE
	
	@echo "✅ All steps completed successfully!"
	@echo "🎉 Publishing workflow completed successfully!"

# Sửa lại run-android-local-publish để sử dụng hàm mới
.PHONY: run-android-local-publish
run-android-local-publish:
	@echo "Publishing to Maven Local with version $(VERSION_NAME) from VERSION file"
	@export SIGNING_KEY_ID=795690AE && \
	export SIGNING_PASSWORD=track-asia && \
	export SIGNING_SECRET_KEY_RING_FILE=$(shell pwd)/signing-key.gpg && \
	./gradlew -PversionName=$(VERSION_NAME) publishToMavenLocal -Psigning.keyId=795690AE -Psigning.password=track-asia -Psigning.secretKeyRingFile=$(shell pwd)/signing-key.gpg
	@echo "Cleaning up metadata..."
	$(MAKE) cleanup-metadata
	@echo "Ký tất cả các artifacts..."
	$(MAKE) sign-all-artifacts GPG_KEY_ID=795690AE
	@echo "Ký các file iOS..."
	$(MAKE) sign-ios-files GPG_KEY_ID=795690AE
	@echo "Ký các file navigation-ui-android..."
	$(MAKE) sign-navigation-ui-android GPG_KEY_ID=795690AE
	@echo "✅ Hoàn tất xuất bản lên Maven Local, dọn dẹp metadata và ký các artifacts."

# Cleanup metadata files after publishing
.PHONY: cleanup-metadata
cleanup-metadata:
	@echo "🧹 Cleaning up metadata files..."
	find ~/.m2/repository/io/github/track-asia -name "*maven-metadata-local.xml*" -delete
	find ~/.m2/repository/io/github/track-asia -name "*metadata-local*" -delete
	@echo "✅ Metadata cleanup completed"



# Mục tiêu mới để ký file AAR của navigation-ui-android
.PHONY: sign-navigation-ui-android
sign-navigation-ui-android:
	@echo "🔏 Ký các file navigation-ui-android..."
	@if [ -z "$(GPG_KEY_ID)" ]; then \
		echo "❌ Vui lòng cung cấp ID khóa GPG bằng cách thêm GPG_KEY_ID=<id_khóa>"; \
		exit 1; \
	fi
	
	@MAVEN_REPO=~/.m2/repository/io/github/track-asia
	@VERSION="$(VERSION_NAME)"
	@echo "   Phiên bản: $$VERSION"
	
	@echo "📋 Kiểm tra khóa GPG $(GPG_KEY_ID)..."
	@if ! gpg --list-keys $(GPG_KEY_ID) > /dev/null 2>&1; then \
		echo "❌ Không tìm thấy khóa GPG $(GPG_KEY_ID) trên hệ thống"; \
		exit 1; \
	fi
	
	@echo ""
	@echo "📋 Ký các file navigation-ui-android:"
	
	@# Kiểm tra và copy file AAR nếu cần thiết
	@if [ ! -f "$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.aar" ]; then \
		echo "   ⚙️ Sao chép file AAR từ build output..."; \
		mkdir -p "$$MAVEN_REPO/navigation-ui-android/$$VERSION/"; \
		cp libandroid-navigation-ui/build/outputs/aar/libandroid-navigation-ui-release.aar "$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.aar"; \
		echo "   ✅ Đã sao chép file AAR"; \
	fi
	
	@# Thiết lập POM file nếu cần
	@if grep -q "<packaging>pom</packaging>" "$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.pom" 2>/dev/null; then \
		echo "   ⚙️ Cập nhật file POM sang packaging aar..."; \
		sed -i '' 's/<packaging>pom<\/packaging>/<packaging>aar<\/packaging>/' "$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.pom"; \
		echo "   ✅ Đã cập nhật file POM"; \
	fi
	
	@FILES=( \
		"$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.pom" \
		"$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION.aar" \
		"$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION-javadoc.jar" \
		"$$MAVEN_REPO/navigation-ui-android/$$VERSION/navigation-ui-android-$$VERSION-sources.jar" \
	)
	
	@COUNT=0
	@TOTAL=0
	
	@for file in "$${FILES[@]}"; do \
		TOTAL=$$((TOTAL+1)); \
		if [ -f "$$file" ]; then \
			echo "   Ký file: $$(basename $$file)"; \
			\
			md5sum "$$file" | cut -d ' ' -f 1 > "$$file.md5"; \
			chmod 644 "$$file.md5"; \
			echo "   ✓ Đã tạo MD5: $$(cat $$file.md5)"; \
			\
			sha1sum "$$file" | cut -d ' ' -f 1 > "$$file.sha1"; \
			chmod 644 "$$file.sha1"; \
			echo "   ✓ Đã tạo SHA1: $$(cat $$file.sha1)"; \
			\
			gpg --batch --yes --passphrase="track-asia" --use-agent --local-user $(GPG_KEY_ID) --armor --detach-sign "$$file"; \
			chmod 644 "$$file.asc"; \
			echo "   ✓ Đã ký GPG"; \
			\
			COUNT=$$((COUNT+1)); \
		else \
			echo "   ⚠️ File không tồn tại: $$(basename $$file)"; \
		fi; \
	done
	
	@echo ""
	@echo "📊 Tổng kết:"
	@echo "  • Tổng số file đã xử lý: $$TOTAL"
	@echo "  • Số file đã ký: $$COUNT"
	
	@if [ $$COUNT -eq 0 ]; then \
		echo "❌ Không có file navigation-ui-android nào được ký. Vui lòng chạy 'make run-android-local-publish' trước khi ký."; \
		exit 1; \
	else \
		echo "✅ Đã ký $$COUNT file navigation-ui-android thành công."; \
		if [ $$COUNT -lt $$TOTAL ]; then \
			echo "⚠️ Cảnh báo: Có $$(( TOTAL - COUNT )) file không tồn tại."; \
		fi; \
	fi

# Thêm vào phần help
.PHONY: help
help:
	@echo "TrackAsia Navigation Android Makefile Commands:"
	@echo ""
	@echo "Publishing commands:"
	@echo "  make run-android-local-publish    - Publish artifacts to Maven Local repository"
	@echo "  make cleanup-metadata             - Clean up Maven metadata files after publishing"
	@echo "  make sign-all-artifacts           - Sign artifacts with MD5, SHA1, and GPG (RECOMMENDED)"
	@echo "  make sign-ios-files               - Ký riêng các file iOS bị thiếu signature (iosx64, iosarm64)"
	@echo "  make sign-navigation-ui-android   - Ký các file navigation-ui-android (aar, pom, javadoc, sources)"
	@echo "  make verify-signatures            - Verify MD5 and SHA1 signatures"
	@echo "  make verify-ios-signatures        - Kiểm tra chữ ký các file iOS đã ký và so sánh với web"
	@echo "  make publish-all                  - Quy trình xuất bản đầy đủ: publish, cleanup, sign, verify"
	@echo "  make show-version                 - Display current version from VERSION file"
	@echo "  make set-version NEW_VERSION=x.y.z - Set a new version in the VERSION file"
	@echo ""


