[ ![Download](https://api.bintray.com/packages/alviannn/maven/LuckyCommons/images/download.svg) ](https://bintray.com/alviannn/maven/LuckyCommons/_latestVersion)

# Lucky Commons

The codes that are commonly used within the Lucky Network project.
The library needs to be compiled within the project.

### How to setup

#### Maven
```xml
<repositories>
    <repository>
        <id>alviannn-bintray</id>
        <url>https://dl.bintray.com/alviannn/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.luckynetwork.alviann.commons</groupId>
        <artifactId>LuckyCommons</artifactId>
        <version>1.0.8</version>
        <type>compile</type>
    </dependency>
</dependencies>
```

#### Gradle (Groovy)
```groovy
repositories {
    maven {
        url = 'https://dl.bintray.com/alviannn/maven'
    }
}

dependencies {
    implementation 'dev.luckynetwork.alviann.commons:LuckyCommons:1.0.8' 
}
```

<br>

If you want to use it in your plugin (either Spigot or Bungee plugin)
make sure to add [LuckyInjector](https://github.com/Alviannn/LuckyInjector/) as the plugin required dependency.

If you don't want to use LuckyInjector, you can use [KtLoader](https://www.spigotmc.org/resources/ktloader.73153/)
but don't forget to also compile the kotlin coroutine dependencies, otherwise it won't work.

Finally, follow the example below. (The sample I'm using is for Spigot, but it should work with BungeeCord too).

##### Java
```java
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        LuckyCommons.loadInjector(this);
    }

}
```
##### Kotlin
```kotlin
class Main : JavaPlugin() {

    override fun onEnable() { 
        LuckyCommons.loadInjector(this)
    }

}
```

<br>

_NOTE: This is just a library and not a plugin!_