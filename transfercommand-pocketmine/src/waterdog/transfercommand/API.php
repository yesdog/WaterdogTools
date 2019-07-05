<?php

namespace waterdog\transfercommand;

use pocketmine\network\mcpe\protocol\ScriptCustomEventPacket;
use pocketmine\Player;
use pocketmine\Server;
use pocketmine\utils\Binary;
use pocketmine\utils\MainLogger;

/**
 * Class API
 * @package waterdog\transfercommand
 * @author TobiasDev
 * @version 1.0
 */
class API
{

    /**
     * @param Player $player
     * @param String $server
     *
     * @return bool
     * Transfers the Player $player to the server $server
     * $player must be on the current server
     */
    public static function transfer(Player $player, String $server): bool
    {

        $pk = new ScriptCustomEventPacket();
        $pk->eventName = "bungeecord:main";
        $pk->eventData = Binary::writeShort(strlen("Connect")) . "Connect" . Binary::writeShort(strlen($server)) . $server;
        $player->sendDataPacket($pk);
        return true;
    }

    /**
     * @param String $player
     * @param String $message
     * @return bool
     * Sends a message $message to the Player $player
     */
    public static function sendMessage(String $player, String $message)
    {
        $sender = Server::getInstance()->getOnlinePlayers()[array_rand(Server::getInstance()->getOnlinePlayers())];
        if ($sender != null && $sender instanceof Player) {
            $pk = new ScriptCustomEventPacket();
            $pk->eventName = "bungeecord:main";
            $pk->eventData = Binary::writeShort(strlen("Message")) . "Message" . Binary::writeShort(strlen($player)) . $player . Binary::writeShort(strlen($message)) . $message;
            $sender->sendDataPacket($pk);
            return true;
        } else {
            MainLogger::getLogger()->warning("You cannot send a message to a player when no player is online on this server!");
            return false;
        }

    }

    /**
     * @param String $player
     * @param String $server
     *
     * @return bool
     * Transfers the player $player to the server $server.
     * $player must be somewhere over the network
     */
    public static function transferOther(String $player, String $server): bool
    {

        $sender = Server::getInstance()->getOnlinePlayers()[array_rand(Server::getInstance()->getOnlinePlayers())];
        if ($sender != null && $sender instanceof Player) {
            $pk = new ScriptCustomEventPacket();
            $pk->eventName = "bungeecord:main";
            $pk->eventData = Binary::writeShort(strlen("ConnectOther")) . "ConnectOther" . Binary::writeShort(strlen($player)) . $player . Binary::writeShort(strlen($server)) . $server;
            $sender->sendDataPacket($pk);
            return true;
        } else {
            MainLogger::getLogger()->warning("You cannot transfer a player when no player is online on this server!");
            return false;
        }
    }
}
