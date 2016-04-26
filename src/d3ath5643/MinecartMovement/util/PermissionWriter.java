package d3ath5643.MinecartMovement.util;

import org.bukkit.entity.EntityType;

public class PermissionWriter {

    private enum Abilities {
        STEER("steer"), JUMP("jump"), CLIMB("climb"), FLY("fly"), MOUNT("mount");
        
        private String value;
        
        private Abilities(String val)
        {
            value = val;
        }
        
        @Override
        public String toString()
        {
            return value;
        }
    };
    
    public static void main(String[] args)
    {
        System.out.println("permissions:");
        System.out.println("  MinecartMovement.*:");
        System.out.println("    description: Gives access to all MinecartMovement's commands and abilities");
        System.out.println("    children:");
        System.out.println("      MinecartMovement.reload: true");
        for(EntityType et: EntityType.values())
        {
            //for(Abilities abil: Abilities.values())
                System.out.println("      MinecartMovement." + et.toString().toLowerCase() + ".*" + ""/*abil.toString()*/ + ": true");
            if(et.toString().contains("MINECART"))
                System.out.println("      MinecartMovement." + et.toString().toLowerCase() + ".groundPlace: true");
        }
        System.out.println("  MinecartMovement.reload:");
        System.out.println("    description: Gives access to the command MinecartMovement reload command");
        System.out.println("    default: op");
        for(EntityType et: EntityType.values())
        {
            System.out.println("  MinecartMovement." + et.toString().toLowerCase().toLowerCase() + ".*:");
            System.out.println("    description: Gives access to all abilities for a " + et.toString().toLowerCase() + ".");
            System.out.println("    children:");
            for(Abilities abil: Abilities.values())
                System.out.println("      MinecartMovement." + et.toString().toLowerCase() + "." + abil.toString() + ": true");
            if(et.toString().contains("MINECART"))
                System.out.println("      MinecartMovement." + et.toString().toLowerCase() + ".groundPlace: true");
            for(Abilities abil: Abilities.values())
            {
                System.out.println("  MinecartMovement." + et.toString().toLowerCase() + "." + abil.toString() + ":");
                System.out.println("    description: Gives access to the ability " + abil.toString() + " for a " + et.toString().toLowerCase() + ".");
                System.out.println("    default: op");
            }
            if(et.toString().contains("MINECART"))
            {
                System.out.println("  MinecartMovement." + et.toString().toLowerCase() + ".groundPlace:");
                System.out.println("    description: Gives access to the ability to place a " + et.toString().toLowerCase() + " on the ground.");
                System.out.println("    default: op");
            }
        }
    }
    
}
