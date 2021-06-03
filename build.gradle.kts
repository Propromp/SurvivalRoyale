import de.undercouch.gradle.tasks.download.Download

plugins {
    java
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val pluginId: String by extra
val pluginName: String by extra
val pluginVersion: String by extra
val pluginPackage: String by extra
val paperVersion: String by extra
val serverDirectory: String by extra
val serverEntry: String by extra

group = "${pluginPackage}.${pluginId}"
version = pluginVersion

repositories {
    mavenCentral()
    jcenter()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://kotlin.bintray.com/kotlinx/")
    maven("https://jitpack.io")
    mavenLocal()
    maven ("https://raw.githubusercontent.com/JorelAli/CommandAPI/mvn-repo/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "latest.release")
    compileOnly("com.destroystokyo.paper", "paper-api", paperVersion)
    compileOnly("org.spigotmc","spigot",paperVersion)
    implementation("dev.jorel:commandapi-shade:5.7")
}

tasks {
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("*.yml") {
            expand(project.properties)
        }

        from(sourceSets.main.get().resources.srcDirs) {
            filter {
                it.replace("@version@", pluginVersion)
            }
        }
    }

    shadowJar {
        archiveFileName.set("$pluginName v$pluginVersion.jar")
    }

    create<Copy>("buildPlugin") {
        File("$serverDirectory/plugins").listFiles()?.forEach{
            if(it.name.contains(pluginName)&&it.isFile){
                File("build/oldbuilds").mkdir()
                it.copyTo(File("build/oldbuilds/${it.name}"),true, DEFAULT_BUFFER_SIZE)
                it.delete()
            }
        }
        from(shadowJar)
        into("$serverDirectory/plugins")
    }

    create<DefaultTask>("setupWorkspace") {
        doLast {

            println("downloading Paper...")
            val paperDir = File(rootDir, serverDirectory)

            paperDir.mkdirs()

            val download by registering(Download::class) {
                src("https://papermc.io/api/v2/projects/paper/versions/1.15.2/builds/391/downloads/paper-1.15.2-391.jar")
                dest(paperDir)
            }
            val paper = download.get().outputFiles.first()

            download.get().download()

            println("setting up paper...")
            runCatching {
                javaexec {
                    workingDir(paperDir)
                    main = "-jar"
                    args("./${paper.name}", "nogui")
                }

                val eula = File(paperDir, "eula.txt")
                eula.writeText(eula.readText(Charsets.UTF_8).replace("eula=false", "eula=true"), Charsets.UTF_8)
                val serverProperties = File(paperDir, "server.properties")
                serverProperties.writeText(serverProperties.readText(Charsets.UTF_8).replace("online-mode=true", "online-mode=false"), Charsets.UTF_8)
            }.onFailure {
                it.printStackTrace()
            }

            println("setting up src folder")
            val file = File("src/main/resources/plugin.yml")
            File("src/main/resources").mkdirs()
            file.createNewFile()
            var apiversion = ""
            for(i in 0..3){
                apiversion += paperVersion.toList()[i]
            }
            file.writeText(
                "name: ${pluginName}\n"+
                        "version: @version@\n"+
                        "main: ${pluginPackage}.${pluginId}.${pluginName}\n"+
                        "api-version: ${apiversion}\n",
                Charsets.UTF_8
            )

            val javaFile = File("src/main/kotlin/${pluginPackage.replace(".","/")}/${pluginId}/${pluginName}")
            File("src/main/kotlin/${pluginPackage.replace(".","/")}/${pluginId}").mkdirs()
            javaFile.createNewFile()
            javaFile.writeText(
                "package ${pluginPackage}.${pluginId}\n\n"+
                        "import org.bukkit.plugin.java.JavaPlugin\n\n"+
                        "class ${pluginName} : JavaPlugin() {\n"+
                        "\toverride fun onEnable() {\n\t\t//起動処理\n\t}\n"+
                        "\toverride fun onDisable() {\n\t\t//停止処理\n\t}\n}"
            )

            println("complete.")
        }
    }
}