/*
 * Copyright (c) 2016, marlonlom
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
plugins {
    id("java")
    id("kotlin")
    id("org.jetbrains.dokka")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

//dokka { //хз как это переписать, да и нужно ли?
//    outputFormat = "html"
//    outputDirectory = "$buildDir/javadoc"
//    jdkVersion = 6
//    skipDeprecated = false
//}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(dokka)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
    }
}

dependencies {
    val kotlin_version = project.rootProject.ext["kotlin_version"]
    val jakeWhartonInstaTime = project.rootProject.ext["jakeWhartonInstaTime"]
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("com.jakewharton.threetenabp:threetenabp:$jakeWhartonInstaTime")
}


