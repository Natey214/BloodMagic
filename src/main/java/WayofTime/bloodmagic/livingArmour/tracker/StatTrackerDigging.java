package WayofTime.bloodmagic.livingArmour.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.livingArmour.LivingArmourUpgrade;
import WayofTime.bloodmagic.api.livingArmour.StatTracker;
import WayofTime.bloodmagic.livingArmour.LivingArmour;
import WayofTime.bloodmagic.livingArmour.upgrade.LivingArmourUpgradeDigging;
import WayofTime.bloodmagic.util.Utils;

public class StatTrackerDigging extends StatTracker
{
    public int totalBlocksDug = 0;

    public static HashMap<LivingArmour, Integer> changeMap = new HashMap<LivingArmour, Integer>();
    public static int[] blocksRequired = new int[] { 128, 512, 1024, 2048, 8192, 16000, 32000, 50000, 80000, 150000 };

    public static void incrementCounter(LivingArmour armour)
    {
        changeMap.put(armour, changeMap.containsKey(armour) ? changeMap.get(armour) + 1 : 1);
    }

    @Override
    public String getUniqueIdentifier()
    {
        return Constants.Mod.MODID + ".tracker.digging";
    }

    @Override
    public void resetTracker()
    {
        this.totalBlocksDug = 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        totalBlocksDug = tag.getInteger(Constants.Mod.MODID + ".tracker.digging");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setInteger(Constants.Mod.MODID + ".tracker.digging", totalBlocksDug);
    }

    @Override
    public boolean onTick(World world, EntityPlayer player, LivingArmour livingArmour)
    {
        if (changeMap.containsKey(livingArmour))
        {
            int change = Math.abs(changeMap.get(livingArmour));
            if (change > 0)
            {
                totalBlocksDug += Math.abs(changeMap.get(livingArmour));

                changeMap.put(livingArmour, 0);

                this.markDirty();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onDeactivatedTick(World world, EntityPlayer player, LivingArmour livingArmour)
    {
        if (changeMap.containsKey(livingArmour))
        {
            changeMap.remove(livingArmour);
        }
    }

    @Override
    public List<LivingArmourUpgrade> getUpgrades()
    {
        List<LivingArmourUpgrade> upgradeList = new ArrayList<LivingArmourUpgrade>();

        for (int i = 0; i < 10; i++)
        {
            if (totalBlocksDug >= blocksRequired[i])
            {
                upgradeList.add(new LivingArmourUpgradeDigging(i));
            }
        }

        return upgradeList;
    }

    @Override
    public double getProgress(LivingArmour livingArmour, int currentLevel)
    {
        return Utils.calculateStandardProgress(totalBlocksDug, blocksRequired, currentLevel);
    }

    @Override
    public boolean providesUpgrade(String key)
    {
        return key.equals(Constants.Mod.MODID + ".upgrade.digging");
    }

    @Override
    public void onArmourUpgradeAdded(LivingArmourUpgrade upgrade)
    {
        if (upgrade instanceof LivingArmourUpgradeDigging)
        {
            int level = upgrade.getUpgradeLevel();
            if (level < blocksRequired.length)
            {
                totalBlocksDug = Math.max(totalBlocksDug, blocksRequired[level]);
                this.markDirty();
            }
        }
    }
}
