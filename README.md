# LevelDb2Avnil
A map converter for Minecraft: Pocket Edition

Map converter for Minecraft:Pocket Edition, from format "LevelDB" to "Anvil", or from format "Anvil" to "LevelDB".

This program uses leveldb by tinfoiled. See lisense here https://github.com/ljyloo/leveldb

This program contains code from Mojang: source at

https://mojang.com/2012/02/new-minecraft-map-format-anvil/

## Usage:

`java -jar Converter.jar <import folder> <export folder> <type>`

## Where:

`<import folder> The full path to the folder containing Minecraft:Pocket Edition world`

`<export folder> The full path to the folder which you want to export`

`<type> 0 represents from "LevelDB" to "Anvil", 1 represents from "Anvil" to "LevelDB"`

## Example:

`java -jar Converter.jar /home/ljyloo/LevelDB /home/ljyloo/Anvil 0`

## Attention:

This is an experimental project, it's highly recommended that you should backup your data before converting. Any data corruption caused by this program should be responsible for yourself.

This is just a stupid converter, what it actually do is just converting the blocks' data, not include other data such as biome, light and entity. If you don't know how to use the files generated by it, don't use it.