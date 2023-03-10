package data.campaign.intel.bar.events;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import data.utils.I18nUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static data.campaign.intel.bar.events.FM_ReimuBarEvent.OptionId.*;

public class FM_ReimuBarEvent extends BaseBarEventWithPerson {


    protected CampaignFleetAPI player_fleet;
    protected PersonAPI officer;
    protected OfficerDataAPI officer_data;

    public static String ReimuKey = "$FM_ReimuCompleted";
    //testing
    //protected PersonAPI contact;

    public enum OptionId {
        INIT,
        TELL_STORY,
        TRY_TO_HELP,
        STAGE_1,
        STAGE_2,
        LEAVE,
    }


    public FM_ReimuBarEvent() {
        super();
    }

    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market)) return false;

        if (!market.getFactionId().equals("fantasy_manufacturing")) {
            return false;
        }

        if (Global.getSector().getPlayerStats().getLevel() < 0 && !DebugFlags.BAR_DEBUG) return false;

        return true;
    }

    @Override
    protected void regen(MarketAPI market) {
        if (this.market == market) return;
        super.regen(market);
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Reimu"));
        person.setName(new FullName("Hakurei", "Reimu", FullName.Gender.FEMALE));
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        regen(dialog.getInteractionTarget().getMarket());

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_BarScene"));

        Color R;
        R = new Color(203, 29, 29, 255);

        dialog.getOptionPanel().addOption(I18nUtil.getString("event","FM_ReimuBarEvent_BarScene_Option"), this,
                R, null);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);


        done = false;

        dialog.getVisualPanel().showPersonInfo(person, true);

        optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof FM_ReimuBarEvent.OptionId)) {
            return;
        }
        FM_ReimuBarEvent.OptionId option = (FM_ReimuBarEvent.OptionId) optionData;

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();

        //SetStoryOption.StoryOptionParams story = new SetStoryOption.StoryOptionParams(STAGE_2,1,"hireMerc",Sounds.STORY_POINT_SPEND_INDUSTRY,"主角光环时刻");
        //SetStoryOption.BaseOptionStoryPointActionDelegate spd = new SetStoryOption.BaseOptionStoryPointActionDelegate(dialog,story);
        //SetStoryOption.set(dialog,story,spd);


        //"涉及到东方内容的时候大可把所有剧情bug的修补推给八云紫".jpg

        switch (option) {
            case INIT:
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_INIT_TEXT_0"));
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_INIT_TEXT_1"));
                if (Global.getSector().getFaction("fantasy_manufacturing").getRelToPlayer().getRel() > 0.15f) {
                    options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_INIT_TRY_TO_HELP"), TRY_TO_HELP);
                }
                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_INIT_LEAVE"), OptionId.LEAVE, I18nUtil.getString("event","FM_ReimuBarEvent_INIT_LEAVE_TIPS"));
                break;
            case TELL_STORY:
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_TEXT_0"));
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_TEXT_1"));
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_TEXT_2"));
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_TEXT_3"));

                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_LEAVE"), OptionId.LEAVE);
                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_STAGE_2"), STAGE_2,
                        I18nUtil.getString("event","FM_ReimuBarEvent_TELL_STORT_STAGE_2_TIPS"));


                BarEventManager.getInstance().notifyWasInteractedWith(this);

                break;

            case STAGE_2:

                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_2_TEXT_0"));
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_2_TEXT_1"));

                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_2_TEXT_2"), new Color(203, 29, 29, 255));

                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_2_LEAVE"), LEAVE, I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_2_LEAVE_TIPS"));

                //军官生成相关内容

                List<String> reimu = new ArrayList<>();
                reimu.add(Skills.TARGET_ANALYSIS);
                reimu.add(Skills.HELMSMANSHIP);
                reimu.add("FM_Reimu_skill");

                //testing
                //doExtraConfirmActions();
                if (!Global.getSector().getMemoryWithoutUpdate().contains(ReimuKey)){
                    Global.getSector().getMemoryWithoutUpdate().set(ReimuKey, true);
                }

                officer = Global.getFactory().createPerson();


                officer_data = Global.getFactory().createOfficerData(officer);


                for (String reimu_skill : reimu) {
                    officer.getStats().setSkillLevel(reimu_skill, 3);
                }
                OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");

                officer.getStats().addXP(plugin.getXPForLevel(1));

                officer.setPersonality(Personalities.AGGRESSIVE);
                officer.setName(person.getName());
                officer.setPortraitSprite(person.getPortraitSprite());
                officer.setGender(person.getGender());


                player_fleet = Global.getSector().getPlayerFleet();
                player_fleet.getFleetData().addOfficer(officer);

                break;

            case TRY_TO_HELP:
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_TRY_TO_HELP_TEXT"));
                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_TRY_TO_HELP_STAGE_1"), OptionId.STAGE_1);
                break;

            case STAGE_1:
                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_1_TEXT_0"));

                text.addPara(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_1_TEXT_1"));
                options.addOption(I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_1_TELL_STORY"),
                        TELL_STORY, I18nUtil.getString("event","FM_ReimuBarEvent_STAGE_1_TELL_STORY_TIPS"));
                break;

            case LEAVE:
                noContinue = true;
                done = true;
                break;

        }
    }

    @Override
    protected String getPersonFaction() {
        return "fantasy_manufacturing";
    }

    @Override
    protected String getPersonRank() {
        return Ranks.SPACE_SAILOR;
    }

    @Override
    protected String getPersonPost() {
        return Ranks.CITIZEN;
    }

    @Override
    protected String getPersonPortrait() {
        return null;
    }

    @Override
    protected FullName.Gender getPersonGender() {
        return FullName.Gender.ANY;
    }

    /*
    protected void doExtraConfirmActions() {
        //testing
        person.setName(new FullName("TEST","TEST", FullName.Gender.FEMALE));
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Reimu"));
        person.setImportanceAndVoice(PersonImportance.VERY_HIGH,random);
        person.addTag(Tags.CONTACT_TRADE);
        person.setFaction("fantasy_manufacturing");
        person.setMarket(market);

        ContactIntel.getContactIntel(person);

        Global.getLogger(this.getClass()).info(person.getNameString());
        Global.getLogger(this.getClass()).info(person.getMarket().getMemoryWithoutUpdate().getBoolean(ContactIntel.NO_CONTACTS_ON_MARKET));
        Global.getLogger(this.getClass()).info(person.getFaction().getCustomBoolean(Factions.CUSTOM_NO_CONTACTS));

        ContactIntel.addPotentialContact(1f,person,person.getMarket(), dialog.getTextPanel());
    }

     */


}
