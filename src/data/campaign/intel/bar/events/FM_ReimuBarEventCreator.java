package data.campaign.intel.bar.events;


import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;


public class FM_ReimuBarEventCreator extends BaseBarEventCreator {

    public FM_ReimuBarEvent createBarEvent() {
        return new FM_ReimuBarEvent();
    }

    public boolean isPriority() {
        return true;
    }

    public float getBarEventFrequencyWeight() {
        return 100f;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        //return 120f + (float) Math.random() * 120f;
        return 10000000000f; // will reset when intel ends... or not, if keeping this one-time-only
    }


}
