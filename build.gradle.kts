// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply{
        set("kotlin_version", "1.5.10")
        set("nav_version", "2.3.5")
        set("jakeWhartonInstaTime", "1.3.0")
        set("jUnitVersion", "4.13.1")
    }
    val kotlin_version: String by extra
    val nav_version: String by extra
    val tools_version = "4.2.1"
    val gms_version = "4.3.5"
    val dokka_version = "0.9.17"

    repositories {
        google()
        mavenCentral()
        maven (url="https://jitpack.io")
        maven (url="https://artifactory.appodeal.com/appodeal")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${tools_version}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.6.1")
        classpath("com.google.gms:google-services:$gms_version")
        classpath("org.jetbrains.dokka:dokka-android-gradle-plugin:$dokka_version")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}



allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}



