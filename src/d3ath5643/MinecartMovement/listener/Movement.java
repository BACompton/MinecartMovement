package d3ath5643.MinecartMovement.listener;

import java.lang.reflect.Field;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import d3ath5643.MinecartMovement.Main;

public class Movement implements Listener{
    private Main plugin;
    
    private enum MatToEntityType
    {
        MINECART(Material.MINECART, EntityType.MINECART),
        MINECART_CHEST(Material.STORAGE_MINECART, EntityType.MINECART_CHEST),
        MINECART_FURNACE(Material.POWERED_MINECART, EntityType.MINECART_FURNACE),
        MINECART_HOPPER(Material.HOPPER_MINECART, EntityType.MINECART_HOPPER),
        MINECART_COMMNAD(Material.COMMAND_MINECART, EntityType.MINECART_COMMAND),
        MINECART_TNT(Material.EXPLOSIVE_MINECART, EntityType.MINECART_TNT);
        
        private Material mat;
        private EntityType et;
        
        private MatToEntityType(Material mat, EntityType et)
        {
            this.mat = mat;
            this.et = et;
        }
        
        public EntityType getEntityType()
        {
            return et;
        }
        
        public static MatToEntityType getFromMat(Material m)
        {
            for(MatToEntityType met : MatToEntityType.values())
                if(met.mat == m)
                    return met;
            return null;
        }
    }
    
    public Movement(Main plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        addPacketListeners();
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent e)
    {
        if(e.getCause() == DamageCause.FALL &&
                ((e.getEntity().getPassenger() != null && e.getEntity().getPassenger().getType() == EntityType.PLAYER)
                        || e.getEntityType() == EntityType.PLAYER && e.getEntity().getVehicle() != null))
        {
            Player p = null;
            Entity vehicle = null;
            
            if(e.getEntityType() == EntityType.PLAYER)
            {
                p = (Player) e.getEntity();
                vehicle = e.getEntity().getVehicle();
            }
            else
            {
                p = (Player) e.getEntity().getPassenger();
                vehicle = e.getEntity();
            }
            
            if(plugin.allowFlight(vehicle.getType()) 
                    && p.hasPermission("MinecartMovement." + vehicle.getType() + ".fly"))
            {
                e.setDamage(0);
                e.setCancelled(true);
                return;
            }
            
            if(plugin.canJump(vehicle) 
                    && p.hasPermission("MinecartMovement." + vehicle.getType() + ".jump"))
            {
                double damage = e.getDamage();
                
                damage -= plugin.getIgnoreDamage(vehicle.getType());
                
                if(damage >= 1)
                    e.setDamage(damage);
                else
                {
                    e.setDamage(0);
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractBlock(PlayerInteractEvent e)
    {
        if(e.getItem() != null && MatToEntityType.getFromMat(e.getItem().getType()) != null
                && e.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            MatToEntityType met = MatToEntityType.getFromMat(e.getItem().getType());
            
            if(plugin.groundPlace(met.getEntityType())
                    && e.getPlayer().hasPermission("MinecartMovement." + met.getEntityType().toString() + "groundPlace")
                    && e.getClickedBlock() != null && isValidBlockFace(e.getClickedBlock(), e.getBlockFace()))
            {
                Location loc = e.getClickedBlock().getRelative(BlockFace.UP).getLocation().add(.5, 0, .5);
                EntityType et = EntityType.valueOf(met.getEntityType().toString());
                Entity ent = e.getPlayer().getWorld().spawnEntity(loc, et);
                
                if(e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName())
                    ent.setCustomName(e.getItem().getItemMeta().getDisplayName());
                removeItem(e.getPlayer(), e.getItem());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if(e.getRightClicked() != null && !e.getPlayer().isSneaking() && plugin.allowMount(e.getRightClicked().getType(), e.getPlayer().getItemInHand())
                && e.getRightClicked().getPassenger() == null)
        {
            if(e.getPlayer() != null && e.getRightClicked() != null
                    && !e.getPlayer().hasPermission("MinecartMovement." + e.getRightClicked().getType().toString().toLowerCase() + ".mount"))
                return;
            
            if(e.getRightClicked() instanceof Pig && plugin.requireSaddle(e.getRightClicked().getType()) && !((Pig)e.getRightClicked()).hasSaddle())
                return;
            e.getRightClicked().setPassenger(e.getPlayer());
            e.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e)
    {
        if(plugin.overideDefaultMountability(e.getVehicle().getType()) && e.getEntered() instanceof Player)
        {
            Player p = (Player) e.getEntered();
            if(!plugin.allowMount(e.getVehicle().getType(), p.getItemInHand()) || !p.hasPermission("MinecartMovement." + e.getVehicle().getType().toString() + ".mount"))
                e.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onVehicleBlockCollision(VehicleBlockCollisionEvent e)
    {
        Location diff = e.getBlock().getLocation().subtract(e.getVehicle().getLocation().getBlockX(),
                                                             e.getVehicle().getLocation().getBlockY(),
                                                             e.getVehicle().getLocation().getBlockZ());
        if(plugin.canClimb(e.getVehicle().getType(), e.getBlock().getRelative(-diff.getBlockX(), -diff.getBlockY(), -diff.getBlockZ()).getType()) ||
                plugin.canClimb(e.getVehicle().getType(), e.getBlock().getType()))
        {
            if(e.getVehicle().getPassenger() != null && e.getVehicle().getPassenger().getType() == EntityType.PLAYER
                    && !e.getVehicle().getPassenger().hasPermission("MinecartMovement." + e.getVehicle().getType().toString().toLowerCase() + ".climb"))
                return;
            
            Vector climbVec = new Vector(e.getVehicle().getVelocity().getX(),
                                         plugin.getClimbVec(e.getVehicle().getType()),
                                         e.getVehicle().getVelocity().getZ());
            e.getVehicle().setVelocity(climbVec);
        }
    }
    
    private void addPacketListeners()
    {
        plugin.getPM().addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Client.STEER_VEHICLE)
                {
                    @Override
                    public void onPacketReceiving(PacketEvent e)
                    {
                        if(e.getPacketType() == PacketType.Play.Client.STEER_VEHICLE)
                        {
                           PacketContainer pc = e.getPacket();
                           double sideways = pc.getFloat().read(0);
                           double forward = pc.getFloat().read(1);
                           boolean jump = pc.getBooleans().read(0);
                           
                           if(e.getPlayer().getVehicle() == null)
                               return;
                           
                           if(Movement.this.plugin.canSteer(e.getPlayer().getVehicle().getType()) &&
                              (sideways != 0 || forward != 0))
                           {
                               Player p = e.getPlayer();
                               Entity v = p.getVehicle();
                               double pDirX = p.getLocation().getDirection().getX(), 
                                       pDirZ = p.getLocation().getDirection().getZ();
                               Vector newVec;
                               
                               if(e.getPlayer() != null && !e.getPlayer().hasPermission("MinecartMovement." + e.getPlayer().getVehicle().getType().toString().toLowerCase() + ".steer"))
                                   return;
                               
                               pDirX *= Movement.this.plugin.getGroundVelocityRatio(v.getType());
                               pDirZ *= Movement.this.plugin.getGroundVelocityRatio(v.getType());
                               
                               if(!v.isOnGround())
                               {
                                   pDirX *= Movement.this.plugin.getAirVelocityRatio(v.getType());
                                   pDirZ *= Movement.this.plugin.getAirVelocityRatio(v.getType());
                               }
                               
                               newVec = new Vector(forward*pDirX + sideways*pDirZ, v.getVelocity().getY(), forward*pDirZ - sideways*pDirX);
                               v.setVelocity(newVec);
                               
                               String className = "";
                               
                               for(int i = 0; i < v.getClass().getName().split("\\.").length-1; i++)
                                   className += v.getClass().getName().split("\\.")[i] + ".";
                               className += "CraftEntity";
                               
                               try {
                                if(Class.forName(className).isAssignableFrom(v.getClass()))
                                   for(Field field: Class.forName(className).getDeclaredFields())
                                   {
                                       if(field.getType().getSimpleName().equals("Entity"))
                                       {
                                               field.setAccessible(true);
                                               Object entity = field.get(v);
                                               for(Field entityField: entity.getClass().getFields())
                                                   if(entityField.getName().equals("yaw"))
                                                   {
                                                       entityField.setAccessible(true);
                                                       entityField.set(entity, p.getLocation().getYaw());
                                                       field.set(v, entity);
                                                   }
                                       }
                                   }
                               } catch (ClassNotFoundException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                                   e1.printStackTrace();
                               }
                           }
                           if(e.getPlayer() != null &&  e.getPlayer().hasPermission("MinecartMovement." + e.getPlayer().getVehicle().getType().toString().toLowerCase() + ".fly")
                                   && Movement.this.plugin.allowFlight(e.getPlayer().getVehicle().getType()))
                           {
                               Entity v = e.getPlayer().getVehicle();
                               if(jump)
                               {
                                   if(e.getPlayer().getLocation().getDirection().getY() >= 0)
                                       v.setVelocity(new Vector(v.getVelocity().getX(), Movement.this.plugin.getAsscendVec(v.getType()), v.getVelocity().getZ()));
                                   else
                                       v.setVelocity(new Vector(v.getVelocity().getX(), Movement.this.plugin.getDecendVec(v.getType()), v.getVelocity().getZ()));
                               }
                               else
                                   v.setVelocity(new Vector(v.getVelocity().getX(), Movement.this.plugin.getHoverVec(v.getType()), v.getVelocity().getZ()));
                               
                           }
                           else if(Movement.this.plugin.canJump(e.getPlayer().getVehicle()) && jump)
                           {
                               if(e.getPlayer() != null && !e.getPlayer().hasPermission("MinecartMovement." + e.getPlayer().getVehicle().getType().toString().toLowerCase() + ".jump"))
                                   return;
                               
                               Entity v = e.getPlayer().getVehicle();
                               v.setVelocity(new Vector(v.getVelocity().getX(), Movement.this.plugin.getJumpVec(v.getType()), v.getVelocity().getZ()));
                           }
                               
                        }
                    }
                });
    }
    
    private boolean isValidBlockFace(Block b, BlockFace bf)
    {
        if(bf == BlockFace.UP && b.getRelative(bf).isEmpty())
            return true;
        return b.getRelative(bf).isEmpty() && !b.getRelative(bf).getRelative(BlockFace.DOWN).isEmpty();
    }
    
    private void removeItem(Player p, ItemStack item)
    {
        if(p.getGameMode() != GameMode.CREATIVE)
        {
            if(item.getAmount() > 1)
            {
                item.setAmount(item.getAmount());
                return;
            }
            if(p.getItemInHand().equals(item))
                p.setItemInHand(new ItemStack(Material.AIR));
            else
                p.getInventory().clear(p.getInventory().first(item));
        }
    }
}
