plugins {
    id("com.android.application")
}

android {
    namespace = "com.panda3ds.pandroid"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.panda3ds.pandroid"
        minSdk = 24
        targetSdk = 33
        versionCode = getGitVersionCode()
        versionName = getGitVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("x86_64", "arm64-v8a")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1")
}

/**
 * Returns the version name based on the current git state
 * If HEAD is a tag, the tag name is used as the version name
 * e.g. `1.0.0`
 * If HEAD is not a tag, the tag name, the branch name and the short commit hash are used
 * e.g. `1.0.0-master-ab00cd11`
 */
def getGitVersionName() {
    def versionName = '0.0.0'

    try {
        // Check if HEAD is a tag
        def process = 'git describe --exact-match'.execute([], project.rootDir)
        def isTag = process.waitFor() == 0

        // Use the tag name as the version name
        def tag = 'git describe --abbrev=0'.execute([], project.rootDir).text.trim()
        if (!tag.isEmpty())
            versionName = tag

        // If HEAD is not a tag, append the branch name and the short commit hash
        if (!isTag)
            versionName += '-' + getGitBranch() + '-' + getGitShortHash()
    } catch (Exception e) {
        logger.quiet(e + ': defaulting to dummy version number ' + versionName)
    }

    logger.quiet('Version name: ' + versionName)
    return versionName
}

/**
 * Returns the number of commits until the last tag
 */
def getGitVersionCode() {
    def versionCode = 1

    try {
        versionCode = Integer.max('git rev-list --first-parent --count --tags'.execute([], project.rootDir).text
                .toInteger(), versionCode)
    } catch (Exception e) {
        logger.error(e + ': defaulting to dummy version code ' + versionCode)
    }

    logger.quiet('Version code: ' + versionCode)
    return versionCode
}

/**
 * Returns the short commit hash
 */
def getGitShortHash() {
    def gitHash = '0'

    try {
        gitHash = 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
    } catch (Exception e) {
        logger.error(e + ': defaulting to dummy build hash ' + gitHash)
    }

    return gitHash
}

/**
 * Returns the current branch name
 */
def getGitBranch() {
    def branch = 'unk'

    try {
        branch = 'git rev-parse --abbrev-ref HEAD'.execute([], project.rootDir).text.trim()
    } catch (Exception e) {
        logger.error(e + ': defaulting to dummy branch ' + branch)
    }

    return branch
}
