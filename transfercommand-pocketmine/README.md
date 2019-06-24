# Waterdog-Transfer

#### A very simple Library that adds a /transfer command

####How is it working?
You can run `/transfer [ServerName]` to teleport to the server you specified. <br /><br />
Notice: **Nothing will happen if that server is not registered in the BungeeCord System!**

You can also run `/transfer [PlayerName] [Server-Name]` to teleport the given player to the given server.
Notice: **Nothing will happen if that server is not registered in the BungeeCord System or the player you specified is not online or not connected through the proxy!**

###Developer-API

This Plugin adds a very simple plugin API, located in the API.php.

Simple Explanation:

API::transfer() has two Arguments:
`Player $player` must be an Instanceof Player, defines the Player who will be teleported and uses him to send the packet <br />
`String $server` Defines the Server you want to teleport to.

```php
$player = Server::getInstance()->getPlayerExact("TobiasDev");
if($player != null){
    API::transfer($player, "Factions");
}
```
This example would check whether the player named "TobiasDev" is online on this server, and if yes, it will transfer him to server "Factions".


API::transferOther() has also two Arguments:
`String $player` the name of the player to be teleported
`String $server` the name of the server you want to transfer $player to.

```php
    API::transferOther("TobiasDev", "Factions");
```

This example would teleport player "TobiasDev" to Server Factions. <br /><br />The player **does not need to be online on the current server, he just needs to be somewhere on the network**

To send the packet, this Method selects a random player who is online on the current server to send the packet. If no one is online, Method call will be dropped and `false` returned.



