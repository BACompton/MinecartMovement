package d3ath5643.MinecartMovement;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import d3ath5643.MinecartMovement.commands.Reload;
import d3ath5643.MinecartMovement.listener.Movement;

public class Main extends JavaPlugin{

    private ProtocolManager procolManager;
    
    @Override
    public void onEnable()
    {
        procolManager = ProtocolLibrary.getProtocolManager();
        createConfig();
        registerCommands();
        new Movement(this);
    }
    
    private void registerCommands() {
        //TODO: add command rideable
        getCommand("mmreload").setExecutor(new Reload(this));
    }

    public void createConfig() {
        File config = new File("plugins/MinecartMovement/config.yml");
        if(!config.exists())
        {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    public ProtocolManager getPM()
    {
        return procolManager;
    }
    
    public boolean canSteer(EntityType type)
    {
        if(getConfig().isBoolean(type.toString() + ".allowSteer"))
            return getConfig().getBoolean(type.toString() + ".allowSteer");
        return getConfig().getBoolean("default.allowSteer");
    }
    
    public boolean canJump(Entity e)
    {
        if(getConfig().isBoolean(e.getType().toString() + ".allowJump"))
            return getConfig().getBoolean(e.getType().toString() + ".allowJump") && e.isOnGround();
        return getConfig().getBoolean("default.allowJump") && e.isOnGround();
    }

    public double getGroundVelocityRatio(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".groundVec"))
            return getConfig().getDouble(type.toString() + ".groundVec");
        return getConfig().getDouble("default.groundVec");
    }

    public double getAirVelocityRatio(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".airVecRatio"))
            return getConfig().getDouble(type.toString() + ".airVecRatio");
        return getConfig().getDouble("default.airVecRatio");
    }

    public double getJumpVec(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".jumpVec"))
            return getConfig().getDouble(type.toString() + ".jumpVec");
        return getConfig().getDouble("default.jumpVec");
    }

    public boolean canClimb(EntityType type, Material mat) {
        boolean climb = false;
        if(getConfig().isList(type.toString() + ".climbMaterials"))
            climb = getConfig().getList(type.toString() + ".climbMaterials").contains(mat.toString());
        return climb || (getConfig().isList("default.climbMaterials") && getConfig().getList("default.climbMaterials").contains(mat.toString()));
    }
    
    public double getClimbVec(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".climbVec"))
            return getConfig().getDouble(type.toString() + ".climbVec");
        return getConfig().getDouble("default.climbVec");
    }

    public boolean allowMount(EntityType type, ItemStack is) {
        Material m = Material.AIR;
        if(is != null)
            m = is.getType();
        
        if(getConfig().isBoolean(type.toString() + ".allowMount")){
            if(getConfig().getBoolean(type.toString() + ".allowMount"))
            {
                boolean includeDefault = false;
                
                if(getConfig().isBoolean(type.toString() + ".require.includeDefault"))
                    includeDefault = getConfig().getBoolean(type.toString() + ".require.includeDefault");
                
                return checkMat(type.toString() + ".require", m, includeDefault);
            }
            return false;
        }
        if(getConfig().getBoolean("default.allowMount"))
            return checkMat("default.require", m, false);
        return false;
    }
    
    public boolean allowFlight(EntityType type) {
        if(getConfig().isBoolean(type.toString() + ".allowFlight"))
            return getConfig().getBoolean(type.toString() + ".allowFlight");
        return getConfig().getBoolean("default.allowFlight");
    }

    public double getAsscendVec(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".asscendVec"))
            return getConfig().getDouble(type.toString() + ".asscendVec");
        return getConfig().getDouble("default.asscendVec");
    }
    
    public double getDecendVec(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".decendVec"))
            return getConfig().getDouble(type.toString() + ".decendVec");
        return getConfig().getDouble("default.decendVec");
    }

    public double getHoverVec(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".hoverVec"))
            return getConfig().getDouble(type.toString() + ".hoverVec");
        return getConfig().getDouble("default.hoverVec");
    }
    
    public boolean groundPlace(EntityType type)
    {
        if(getConfig().isBoolean(type.toString() + ".groundPlace"))
            return getConfig().getBoolean(type.toString() + ".groundPlace");
        return getConfig().getBoolean("default.groundPlace");
    }

    public boolean overideDefaultMountability(EntityType type) {
        if(getConfig().isBoolean(type.toString() + ".overrideDefaultMountability"))
            return getConfig().getBoolean(type.toString() + ".overrideDefaultMountability");
        return getConfig().getBoolean("default.overrideDefaultMountability");
    }
    
    public boolean requireSaddle(EntityType type) {
        if(getConfig().isBoolean(type.toString() + ".require.saddle"))
            return getConfig().getBoolean(type.toString() + ".require.saddle");
        return getConfig().getBoolean("default.require.saddle");
    }

    public double getIgnoreDamage(EntityType type) {
        if(getConfig().isDouble(type.toString() + ".ignoreFallDamage"))
            return getConfig().getDouble(type.toString() + ".ignoreFallDamage");
        return getConfig().getDouble("default.ignoreFallDamage");
    }
    
    private boolean checkMat(String path, Material m, boolean inculdeDefault)
    {
        if(getConfig().isList(path + ".can") && !getConfig().getStringList(path + ".can").contains(m.toString())
                && (inculdeDefault && getConfig().isList("default.require.can") && !getConfig().getStringList("default.require.can").contains(m.toString()) || !inculdeDefault))
            return false;
        if(getConfig().isList(path + ".cannot") && getConfig().getStringList(path + ".cannot").contains(m.toString())
                || (inculdeDefault && getConfig().isList("default.require.cannot") && getConfig().getStringList("default.require.cannot").contains(m.toString())))
            return false;
        return true;
    }
}
