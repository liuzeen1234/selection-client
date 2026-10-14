# Selection Client

独立的客户端选区 mod，支持 Minecraft 1.20.4（Java 17）和 26.2（Java 25），需要对应版本的 Fabric Loader 和 Fabric API。从 AI Helper 复制客户端代码，不修改原项目，不依赖原 mod 的 jar 或 debug-menu。

进入世界后按 **K** 打开（可在按键设置中调整）。保留坐标输入、当前位置、确认/清除、高亮、草稿、方块统计、忽略方块设置和最多 10 条选区历史。历史文件为游戏目录下的 `config/selection_client_selections.txt`。

## 本地导出

纯客户端运行，无服务端通信，也不需要服务器安装 AI Helper。仅提供 TXT 蓝图和 NBT 结构导出，均保存到本机；默认目录分别为游戏目录下的 `selection-client/structures/txts/` 和 `selection-client/structures/nbts/`，也可选择本机文件夹。忽略方块设置对两种格式均生效。

导出保存方块位置和方块状态，不包含容器库存、方块实体数据或实体。选区、高亮、记忆、方块分析可独立运行。分析和导出只读取客户端已加载的世界数据，请确保整个选区已加载。

独立 Java 包、资源命名空间、mod ID、历史文件；与原客户端 mod 同时安装时，两者选区独立。

## 构建

参考 `debug_menu` 的 `-Pmc` 目标选择方式，每个 Minecraft 版本分别生成 jar：

```sh
./gradlew build -Pmc=1.20.4
./gradlew build -Pmc=26.2
```

不传 `-Pmc` 时默认构建 1.20.4。安装 `build/libs/selection-client-mc1.20.4-1.0.0.jar` 或 `build/libs/selection-client-mc26.2-1.0.0.jar`，选择与游戏匹配的版本，不要安装 sources jar。

| Minecraft | Java | Fabric Loader | Fabric API |
| --- | --- | --- | --- |
| 1.20.4 | 17 | 0.15.11+ | 0.95.4+1.20.4 |
| 26.2 | 25 | 0.19.3+ | 0.161.0+26.2 |

开发启动：`./gradlew runClient -Pmc=26.2`（或 `-Pmc=1.20.4`）。各版本使用独立的 `run/<版本>/` 测试目录，避免混入其他版本的 mod 或存档。启动前先检查、停止已有项目测试客户端，避免同时运行多个客户端。

构建使用 Gradle 9.5.1 / Loom 1.17.21，Gradle 进程需要 Java 25，1.20.4 目标仍由 Java 17 toolchain 编译。`gradle.properties` 中的本机 JDK 路径与代理配置需按实际环境调整。

### 源码维护

- `src/main/java`：1.20.4，使用 Yarn 映射。
- `src/mc26.2/java`：26.2，使用 Minecraft 未混淆类名，适配新的 GUI 提取、鼠标事件、按键分类和 Gizmo 选区渲染 API。
- 两个目标共用 `src/main/resources`，mod 元数据随构建目标生成，构建中间文件保存在 `build/targets/<版本>`。

26.2 的 API 差异较大，因此按目标选择源码目录。修改选区、历史、分析或导出逻辑时需同步两套实现，并运行上述两条构建命令验证。
