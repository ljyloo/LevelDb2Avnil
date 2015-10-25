# LevelDb2Anvil
A map converter for Minecraft: Pocket Edition

Map converter for Minecraft: Pocket Edition, from format "LevelDB" to "Anvil".

This program uses leveldb by tinfoiled. See lisense here https://github.com/ljyloo/leveldb

This program contains code from Mojang: source at

https://mojang.com/2012/02/new-minecraft-map-format-anvil/

##Warning:

The converter will experience an error if you have blocks which are not supported by Minecraft PC Edition. It will make your .mca files corrupted, therefore please replace those blocks with another block that is supported.

## Usage:

Import your LevelDB world in and rename it to the name 'world'.
Run start.bat(if you're on Windows) or start.sh(if you're on OS X or Linux) and the converter will now convert it to Anvil format to the output folder of 'worldanvil'.
Find more info at https://forums.pocketmine.net/threads/leveldb-maps-to-anvil.12018/

##Credits:
Thanks to @ljyloo for creating this amazing converter.
User-friendly start.bat/start.sh, the folder 'worldanvil' and the rewrite of this file was by @keithkfng.
