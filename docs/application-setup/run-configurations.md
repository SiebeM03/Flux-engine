# Run configurations

You must run the **engine** entry point (**me.siebe.flux.core.FluxLauncher**), not your application class. This page
describes how to configure Maven and IntelliJ IDEA to run a Flux game correctly, including the **Flux_Launcher.xml** run
configuration and optional VM parameters.

See also: [Implementing your application](implementing-your-app.md).

## Main class

| Environment                     | Main class                          |
|---------------------------------|-------------------------------------|
| IntelliJ Application run config | **me.siebe.flux.core.FluxLauncher** |
| Maven exec plugin / shade JAR   | **me.siebe.flux.core.FluxLauncher** |

The launcher discovers and runs your single **FluxApplication** subclass; you never set your game class as the main
class.

## Maven

### Exec plugin

If you use **exec-maven-plugin**, set the main class to **FluxLauncher**:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <systemProperties>
            <systemProperty>
                <key>flux.shader.hotreload.paths</key>
                <value>
                    src/main/resources,flux-renderer-3d/src/main/resources
                </value>
            </systemProperty>
        </systemProperties>
    </configuration>
</plugin>
```

Run with: `mvn exec:java` (from the module that contains your **FluxApplication** and has **flux-core** on the
classpath).

### Shade plugin (runnable JAR)

The demo-game uses the **maven-shade-plugin** to build a single JAR with dependencies and the correct main class in the
manifest:

Add the following **maven-shade-plugin** configuration to your game's pom.xml to build a single JAR with dependencies
and the correct main class in the manifest:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <transformers>
                    <transformer
                            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>me.siebe.flux.core.FluxLauncher</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

After **mvn package**, a game with artifactId `demo-game` would result into 2 JAR files:

- `original-demo-game-<version>.jar` — just your classes, no dependencies, not runnable directly.
- `demo-game-<version>.jar` — the "uber jar" with all dependencies and the correct manifest. **Use
  this JAR to run your game**

If your artifactId is something other than `demo-game`, or your project version is different, substitute those in the
file names and paths.

**Note:** The main class **must be** `me.siebe.flux.core.FluxLauncher` (not your application class). This ensures the
launcher finds and runs your game correctly using classpath scanning.

Replace **demo-game** and **&lt;version&gt;** with your artifact ID and version.

## IntelliJ IDEA

### Application run configuration

Create an **Application** run configuration that uses **FluxLauncher** as the main class and your **game module** as the
module (so the classpath includes your **FluxApplication** subclass and its dependencies).

1. **Run → Edit Configurations**
2. **+ → Application**
3. Set **Name** (e.g. **Flux Launcher**)
4. **Main class:** `me.siebe.flux.core.FluxLauncher`
5. **Module:** Your game module (e.g. **demo-game**)
6. Optionally add **VM options** (see below)

### Flux_Launcher.xml

The same configuration can be stored in version control or shared via **.idea/runConfigurations/Flux_Launcher.xml**.
Example:

```xml
<component name="ProjectRunConfigurationManager">
    <configuration default="false" name="Flux Launcher" type="Application" factoryName="Application">
        <option name="MAIN_CLASS_NAME" value="me.siebe.flux.core.FluxLauncher"/>
        <module name="demo-game"/>
        <option name="VM_PARAMETERS"
                value="-Dflux.shader.hotreload.paths=demo-game/src/main/resources,flux-renderer-3d/src/main/resources"/>
        <method v="2">
            <option name="Make" enabled="true"/>
        </method>
    </configuration>
</component>
```

| Option              | Meaning                                                                                                                                         |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **MAIN_CLASS_NAME** | Must be **me.siebe.flux.core.FluxLauncher**.                                                                                                    |
| **module**          | The module that contains your **FluxApplication** subclass and game code (e.g. **demo-game**). Replace with your game module name if different. |
| **VM_PARAMETERS**   | Optional. Used here for shader hot-reload paths; adjust or omit if you do not use that feature.                                                 |
| **Make**            | Build the project before run; leave enabled unless you have a reason to disable it.                                                             |

If you use a different module name, change **module** and any paths in **VM_PARAMETERS** (e.g. *
*my-game/src/main/resources**).

### VM parameters (optional)

- **Shader hot-reload:** Set **flux.shader.hotreload.paths** to a comma-separated list of directories (relative to the
  working directory) to watch for `.vert` / `.frag` changes. Example:

  ```text
  -Dflux.shader.hotreload.paths=demo-game/src/main/resources,flux-renderer-3d/src/main/resources
  ```

- Other Flux or JVM options can be added in the same **VM_PARAMETERS** string, space-separated.

Working directory is typically the project root when running from IntelliJ. Paths in **flux.shader.hotreload.paths** are
resolved against that directory.

## Dependencies

Your game module must depend on at least **flux-core** (which provides **FluxLauncher** and **FluxApplication**). Add
any other Flux modules you use (e.g. **flux-renderer-3d**, or transitively **flux-lwjgl** for the GLFW window).

Example (demo-game):

```xml
<dependencies>
    <dependency>
        <groupId>me.siebe</groupId>
        <artifactId>flux-core</artifactId>
        <version>${flux.engine.version}</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>me.siebe</groupId>
        <artifactId>flux-renderer-3d</artifactId>
        <version>${flux.engine.version}</version>
    </dependency>
</dependencies>
```

| Artifact             | Purpose                                                   |
|----------------------|-----------------------------------------------------------|
| **flux-core**        | **FluxLauncher**, **FluxApplication**, core lifecycle.    |
| **flux-renderer-3d** | 3D render pipeline, GLTF, render steps (example).         |
| **flux-lwjgl**       | GLFW window implementation; often pulled in transitively. |

Adjust **flux.engine.version** to match your parent or property.

## Summary

| Item                 | Value                                                       |
|----------------------|-------------------------------------------------------------|
| Main class (always)  | **me.siebe.flux.core.FluxLauncher**                         |
| IntelliJ module      | Your game module (e.g. **demo-game**)                       |
| Maven exec mainClass | **me.siebe.flux.core.FluxLauncher**                         |
| Shade JAR mainClass  | **me.siebe.flux.core.FluxLauncher**                         |
| VM option (optional) | **-Dflux.shader.hotreload.paths=...** for shader hot-reload |
