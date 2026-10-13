# Selection Client

独立的客户端选区 mod，适用于 Minecraft 1.20.4、Fabric Loader 0.15+、Fabric API 和 Java 17。从 AI Helper 复制客户端代码，不修改原项目，不依赖原 mod 的 jar 或 debug-menu。

进入世界后按 **K** 打开（可在按键设置中调整）。保留坐标输入、当前位置、确认/清除、高亮、草稿、方块统计、忽略方块设置和最多 10 条选区历史。历史文件为游戏目录下的 `config/selection_client_selections.txt`。

## 本地导出

纯客户端运行，无服务端通信，也不需要服务器安装 AI Helper。仅提供 TXT 蓝图和 NBT 结构导出，均保存到本机；默认目录分别为游戏目录下的 `selection-client/structures/txts/` 和 `selection-client/structures/nbts/`，也可选择本机文件夹。忽略方块设置对两种格式均生效。

导出保存方块位置和方块状态，不包含容器库存、方块实体数据或实体。选区、高亮、记忆、方块分析可独立运行。分析和导出只读取客户端已加载的世界数据，请确保整个选区已加载。

独立 Java 包、资源命名空间、mod ID、历史文件；与原客户端 mod 同时安装时，两者选区独立。

## 构建

```sh
./gradlew build
```

安装产物 `build/libs/selection-client-1.0.0.jar`，不要安装 sources jar。
开发启动：`./gradlew runClient`。启动前先检查、停止已有项目测试客户端，避免同时运行多个客户端。
`gradle.properties` 沿用本机 JDK 17 路径和代理配置，换机器时按实际环境调整。
