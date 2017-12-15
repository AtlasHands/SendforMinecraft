import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Teleport extends JavaPlugin implements Listener {
    public boolean commandUsed = false;
    public boolean chestChosen = false;
    public Player tempGlobal;
    public Chest transport;
    @Override
    public void onEnable () { //When the Server Has been Enabled
        getLogger().info("\u001B[1;33m" + "Send has been Enabled! \u001B[0m"); //Send to the Console, \u001B is a color
        //This Registers this class/plugin as an event listener
        getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    //When disabled just getLogger Saying its disabled
    public void onDisable () {
        getLogger().info("\u001B[1;34m" + "Send has been Disabled! \u001B[0m");
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { //listens for events when player uses a command
        if (cmd.getName().equalsIgnoreCase("send")) { // If the command is send
            sender.sendMessage(ChatColor.BLUE + "[Send] " + ChatColor.WHITE + "Click the chest you want to send with a dirt block"); //colors + response
            tempGlobal = (Player) sender; //make a global player in order to preserve transaction through multiple events - may need to make a linked list?
            commandUsed = true; //this is so that we can now listen for player interact events below if they used the command
            //VV Time delay of 30 seconds to complete teleportation or the process stops itself by setting flags to false
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    if(commandUsed){ //if it was true - set to false -> this also allows us to check if teleport has been completed
                        commandUsed = false;
                        tempGlobal.sendMessage(ChatColor.BLUE +"[Send] " + ChatColor.WHITE + "Ran out of time to teleport!");
                    }

                }
            }, (30 * 20));//apparently 30 is the seconds because of the tick speed being 20
            return true;
            }
        return false;

    }
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent evt){
        if (!evt.hasBlock()){ //if a block is not in the event cancel it
            return;
        }
        if(commandUsed){ //if we are at the first stage
            if(evt.getAction() == Action.LEFT_CLICK_BLOCK) { //if left clicked
                Player player = evt.getPlayer();//just grabbing players
                ItemStack b = evt.getItem();//and grabbing item used to click
                if (b.getType() == (Material.DIRT)) { //if the item used was of type dirt
                    if (evt.getClickedBlock().getType().equals(Material.CHEST)) {//and if the clicked block was a chest
                        Chest ch = (Chest) evt.getClickedBlock().getState();//bring the chest in as variable ch
                        transport = ch; //it will be the transported chest global variable
                        player.sendMessage(ChatColor.BLUE + "[Send] " + ChatColor.WHITE + "Left click a chest you want to use for the teleportation with a stick");
                        chestChosen= true; //move over to the if statement below
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() { //same as above, 30 seconds given for the next process
                            public void run() {
                                if(chestChosen) {
                                    chestChosen = false;
                                    tempGlobal.sendMessage(ChatColor.BLUE +"[Send] " + ChatColor.WHITE + "Ran out of time to teleport!");
                                }
                            }
                        }, (30 * 20));
                        commandUsed = false; //set this to false so we run the below if
                    }
                }
            }
        }
        if(chestChosen){ //if at this stage
            if(evt.getItem().getType() == Material.STICK){ //if the event was done with a stick
                if(evt.getAction() == Action.LEFT_CLICK_BLOCK){//and it was a left click
                    if(evt.getClickedBlock().getType() == Material.CHEST){ //and the click was to a chest
                        Chest chest = (Chest) evt.getClickedBlock().getState(); //bring in the chest
                        ItemStack is[] = transport.getInventory().getContents();//make an itemstack array of all the items in the transport chest
                        transport.getInventory().clear();//clear the transport chest -- security so that this takes place before the transfer
                        chest.getInventory().setContents(is);//set the contents of the new chest to the one we grabbed from transport
                        evt.getPlayer().sendMessage(ChatColor.BLUE + "[Send] " + ChatColor.WHITE + "Teleportation complete!");
                        chestChosen = false;//end the process so we dont get an error message
                    }
                }
            }
        }
    }
}


