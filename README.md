# MinecartMovement
A Spigot plugin that expands on the mounting system in Minecraft.
Once finished, a link to the pre-complied version will be linked _here_.

MinecartMovement allows a player to do the following:
* Mount an entity
* Steer an entity
* Jump with an entity
* Fly with an entity

### Dependicies
* ProtocolLib

### Permissions
* MinecartMovement.*:
  * MinecartMovement.command.*:
    * MinecartMovement.command.reload: Grants access to MinecartMovement's reload command.
  * MinecartMovement._\<entityNeme\>_.*:
    * MinecartMovement._\<entityNeme\>_..steer: Grants the ability to steer that entity.
    * MinecartMovement._\<entityNeme\>_..jump: Grants the ability to jump that entity.
    * MinecartMovement._\<entityNeme\>_..climb: Grants the ability to climb that entity.
    * MinecartMovement._\<entityNeme\>_..fly: Grants the ability to fly that entity.
    * MinecartMovement._\<entityNeme\>_..mount: Grants the ability to mount that entity.

### Commands
* MMreload: Reloads MinecartMovement's config file
  * usage: /mmreload
  * permission: MinecartMovement.command.reload
