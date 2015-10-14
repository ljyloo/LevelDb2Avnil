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
