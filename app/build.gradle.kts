import org.jetbrains.kotlin.konan.properties.Properties
import java.io.*

plugins {
    id("com.android.application")
    id("com.frogermcs.androiddevmetrics")
    id("kotlin-dsl")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("com.gladed.androidgitversion") version("0.4.3")
    id("io.gitlab.arturbosch.detekt") version("1.17.0")

}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://artifactory.appodeal.com/appodeal")
    }
}

fun getProps(path: String): Properties {
    val props = Properties()
    props.load(FileInputStream(file(path)))
    return props
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))


android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.intergroupapplication"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 100004
//        androidGitVersion.code()
        versionName ="1.6.0.9"
//        androidGitVersion.name()
        testInstrumentationRunner ="androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("keystores/debug.keystore")
        }

        getByName("release") {
            try {
                storeFile = file(System.getenv("KEY_PATH"))
                keyAlias = System.getenv("KEY_ALIAS")
                storePassword = System.getenv("STORE_PASSWORD")
                keyPassword = System.getenv("KEY_PASSWORD")
            } catch (exception: Exception) {
                keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"].toString()
                keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"].toString()
                storeFile = file(keystoreProperties["RELEASE_STORE_FILE"].toString())
                storePassword = keystoreProperties["RELEASE_STORE_PASSWORD"].toString()
            }
        }

    }

    flavorDimensions.add("mode")

    productFlavors {

        getByName("develop") {
            dimension = "mode"
            getProps("./config/development.props").forEach { p ->
                buildConfigField("String", p.key.toString(), p.value.toString())
            }
        }
        getByName("develop") {
            dimension = "mode"
            getProps("./config/production.props").forEach { p ->
                buildConfigField("String", p.key.toString(), p.value.toString())
            }
        }

    }

    buildTypes {
        getByName("debug") {
            minifyEnabled(false)
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles = mutableListOf(
                getDefaultProguardFile("proguard-android.txt"),
                file("proguard-rules.pro")
            )
            proguardFiles = fileTree("proguard").toMutableList()
        }

        getByName("release") {
            debuggable(false)
            minifyEnabled(false)
            proguardFiles = mutableListOf(
                getDefaultProguardFile("proguard-android.txt"),
                file("proguard-rules.pro")
            )
            proguardFiles = fileTree("proguard").toMutableList()
            signingConfig = signingConfigs.getByName("release")
        }
    }

    configurations.all {
        exclude("com.android.support", "support-v13")
    }

    packagingOptions {
        exclude("META-INF/library_release.kotlin_module")
    }

    applicationVariants.all(object : Action<com.android.build.gradle.api.ApplicationVariant> {
        override fun execute(variant: com.android.build.gradle.api.ApplicationVariant) {
            println("variant: ${variant}")
            variant.outputs.all(object : Action<com.android.build.gradle.api.BaseVariantOutput> {
                override fun execute(output: com.android.build.gradle.api.BaseVariantOutput) {

                    val outputImpl =
                        output as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                    var apkName = "InterGroup-Application-" + variant.productFlavors.get(0).name
                    apkName += "-" + variant.versionName
                    if (variant.buildType.name == "release") {
                        apkName += "-release"
                    } else if (variant.buildType.name == "debug") {
                        apkName += "-debug"
                    } else {
                        return
                    }
                    apkName += ".apk"
                    outputImpl.outputFileName = apkName
                }
            })
        }
    })

    buildFeatures {
        viewBinding = true
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config = files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
        sarif.enabled = true // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
}


dependencies {
    val kotlin_version = project.rootProject.ext["kotlin_version"]
    val nav_version = project.rootProject.ext["nav_version"]
    val jakeWhartonInstaTime = project.rootProject.ext["jakeWhartonInstaTime"]
    val paging_version = "3.0.0"
    val junitVersion = "4.13.2"
    val mockitoVersion = "2.19.0"
    val mockitoKotlinVersion = "1.5.0"
    val assertVersion = "3.19.0"
    val moxyVersion = "2.2.0"
    val retrofitVersion = "2.9.0"
    val loggingInterceptorVersion = "4.9.0"
    val daggerVersion = "2.35.1"
    val rxJavaVersion = "2.2.6"
    val rxAndroidVersion = "2.1.1"
    val rxBindingVersion = "2.2.0"
    val cryptoLibraryVersion = "1.0.0"
    val rxPaparazzoVersion = "0.6.1-2.x"
    val frescoVersion = "2.4.0"
    val timberVersion = "4.7.1"
    val errorHandlerVersion = "1.1.0"
    val saripaarVersion = "2.0.3"
    val toastyVersion = "1.3.0"
    val loadingLibraryVersion = "2.1.3"
    val datePickerVersion = "3.5.1"
    val materialEditTextVersion = "2.1.4"
    val clansFabVersion = "1.6.4"
    val linkBuilderVersion = "2.0.5"
    val constraintVersion = "1.1.3"
    val testRunnerVersion = "1.0.2"
    val espressoVersion = "3.0.2"
    val jUnitVersion = "4.13.1"
    val googleProgressBarVersion = "1.2.0"
    val snackBarVersion = "1.1.1"
    val firebaseVersion = "18.0.2"
    val firebaseMessagingVersion = "21.0.1"
    val adsVersion = "19.7.0"
    val gmsVersion = "17.2.1"
    val materialDrawerVersion = "1.3.7"
    val circleProgressBarVersion = "1.2.2"
    val androidNetworkingVersion = "1.0.2"
    val compressorVersion = "2.1.0"
    val refreshVersion = "1.2.3@aar"
    //Android dependencies
    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.media:media:1.3.1")
    implementation("androidx.activity:activity-ktx:1.3.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.constraintlayout:constraintlayout:$constraintVersion")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    //Tests
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("com.nhaarman:mockito-kotlin-kt1.1:$mockitoKotlinVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertVersion")

    //Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")

    //Reactive
    implementation("io.reactivex.rxjava2:rxjava:$rxJavaVersion")
    implementation("io.reactivex.rxjava2:rxandroid:$rxAndroidVersion")
    implementation("android.arch.lifecycle:reactivestreams:1.1.1")

    //Network
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion")
    implementation("com.amitshekhar.android:android-networking:$androidNetworkingVersion")

    //DI
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    kapt("com.google.dagger:dagger-android-processor:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")

    //MVP
    implementation("com.github.moxy-community:moxy:$moxyVersion")
    kapt("com.github.moxy-community:moxy-compiler:$moxyVersion")
    implementation("com.github.moxy-community:moxy-android:$moxyVersion")
    implementation("com.github.moxy-community:moxy-androidx:$moxyVersion")
    implementation("com.github.moxy-community:moxy-material:$moxyVersion")
    implementation("com.github.moxy-community:moxy-ktx:$moxyVersion")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
    implementation("android.arch.navigation:navigation-ui:1.0.0")

    //LeackCanary
//    debugImplementation "com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
//    releaseImplementation "com.squareup.leakcanary:leakcanary-android-no-op:$leakCanaryVersion")
//    debugImplementation "com.squareup.leakcanary:leakcanary-support-fragment:$leakCanaryVersion")

    //Validation
    implementation("com.mobsandgeeks:android-saripaar:$saripaarVersion")

    //MaterialUtils
    implementation("com.klinkerapps:link_builder:$linkBuilderVersion")
    implementation("com.github.clans:fab:$clansFabVersion")
    implementation("com.rengwuxian.materialedittext:library:$materialEditTextVersion")
    implementation("co.infinum:materialdatetimepicker-support:$datePickerVersion")
    implementation("com.github.GrenderG:Toasty:$toastyVersion")
    implementation("com.wang.avi:library:$loadingLibraryVersion")
    implementation("com.jpardogo.googleprogressbar:library:$googleProgressBarVersion")
    implementation("com.androidadvance:topsnackbar:$snackBarVersion")
    implementation("co.zsmb:materialdrawer-kt:$materialDrawerVersion")
    implementation("com.budiyev.android:circular-progress-bar:$circleProgressBarVersion")

    //Fresco
    implementation("com.facebook.fresco:fresco:$frescoVersion")
    implementation("com.facebook.fresco:animated-gif:$frescoVersion")
    implementation("com.facebook.fresco:animated-webp:$frescoVersion")
    implementation("com.facebook.fresco:webpsupport:$frescoVersion")

    //Logging
    implementation("com.jakewharton.timber:timber:$timberVersion")

    //ErrorHandling
    implementation("com.workable:error-handler:$errorHandlerVersion")

    //RxBindings
    implementation("com.jakewharton.rxbinding2:rxbinding:$rxBindingVersion")
    implementation("com.jakewharton.rxbinding2:rxbinding-support-v4:$rxBindingVersion")
    implementation("com.jakewharton.rxbinding2:rxbinding-appcompat-v7:$rxBindingVersion")
    implementation("com.jakewharton.rxbinding2:rxbinding-design:$rxBindingVersion")
    implementation("com.jakewharton.rxbinding2:rxbinding-recyclerview-v7:$rxBindingVersion")
    implementation("com.github.miguelbcr:RxPaparazzo:$rxPaparazzoVersion")

    //CryptoLibrary
    implementation("cc.duduhuo.util:digest-util-android:$cryptoLibraryVersion")

    //Adapters
//    implementation "com.mikepenz:fastadapter:$pinceAdapterVersion"
//    implementation "com.mikepenz:fastadapter-commons:$pinceAdapterVersion"
//    implementation "com.mikepenz:fastadapter-extensions:$pinceAdapterVersion"
//    implementation "com.github.CymChad:BaseRecyclerViewAdapterHelper:$chinaAdapterVersion"

    //MultiDex
    implementation("androidx.multidex:multidex:2.0.1")

    // Crashlitycs
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:27.1.0"))

    // Declare the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don"t specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    //Firebase
    implementation("com.google.firebase:firebase-core:$firebaseVersion")
    implementation("com.google.firebase:firebase-messaging:$firebaseMessagingVersion")


    //Ads
    implementation("com.google.android.gms:play-services-ads:$adsVersion")
    implementation("com.google.android.gms:play-services-ads-identifier:17.0.0")
    implementation("com.google.firebase:firebase-ads:$adsVersion")
    implementation("com.google.android.gms:play-services-tasks:$gmsVersion")

    //Compressor
    implementation("id.zelory:compressor:$compressorVersion")

    //Time
    implementation("com.jakewharton.threetenabp:threetenabp:$jakeWhartonInstaTime")
    implementation(project(":ta_library"))

    //RefreshLayout
    implementation("com.github.omadahealth:swipy:${refreshVersion}")

    implementation("com.github.florent37:shapeofview:1.4.7")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    //appodeal
    implementation("com.appodeal.ads:sdk:2.9.2.+")
    implementation("com.explorestack:consent:1.0.2")
    //Video player
    implementation("com.google.android.exoplayer:exoplayer:2.12.2")
    implementation("com.danikula:videocache:2.7.1")

    //paging 3
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")
    implementation("androidx.paging:paging-rxjava2-ktx:$paging_version")

    implementation("com.android.billingclient:billing-ktx:3.0.2")
    implementation("com.google.android.gms:play-services-auth:19.0.0")
    implementation("com.google.firebase:firebase-auth")

    // To use only without reflection variants of viewBinding
    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.4.6")
}
