package it.polimi.ingsw.network.client.ClientModel;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.enums.ResourceType;
import it.polimi.ingsw.model.exceptions.IllegalResourceException;
import it.polimi.ingsw.network.Utilities;
import it.polimi.ingsw.network.client.CLI.enums.Color;
import it.polimi.ingsw.network.client.CLI.enums.Resource;

import java.util.*;

public class ClientBoard {
    private final ClientFaithPath faithPath;
    private final ClientDeposits deposits;
    private final Map<Integer, Stack<DevelopmentCard>> slots = new HashMap<>();
    private Integer totalSlotPoints;
    private Integer totalCardsBought;
    private final List<Resource> pendingResources = new ArrayList<>();
    private List<LeaderCard> leadersInHand = new ArrayList<>();
    private final List<LeaderCard> leadersInBoard = new ArrayList<>();
    private Map<Integer,String> discardedCards=new HashMap<>();
    private Map<Integer,String> playedCards=new HashMap<>();

    private List<Production> activeProductions = new ArrayList<>();
    private Production baseProduction;
    private final List<Production> extraProductions = new ArrayList<>();
    private List<ResourceDiscount> discounts = new ArrayList<>();


    /**
     * initialization of the elements
     */
    public ClientBoard(){
        deposits = new ClientDeposits();
        faithPath = new ClientFaithPath();
        slots.put(1, new Stack<>());
        slots.put(2, new Stack<>());
        slots.put(3, new Stack<>());
        totalSlotPoints = 0;
        totalCardsBought = 0;
        baseProduction = new Production();
        try {
            baseProduction.addInput(ResourceType.UNKNOWN,2);
            baseProduction.addOutput(ResourceType.UNKNOWN,1);
        } catch (IllegalResourceException e) {
            e.printStackTrace();
        }

    }

    public void setDiscounts(List<ResourceDiscount> discounts) {
        this.discounts = discounts;
    }

    /**
     * set the unknown productions (base productions and extra productions)
     * @param unknownProductions the map of the productions
     */
    public void setUnknownProductions(Map<Integer,Production> unknownProductions){
        for (Map.Entry<Integer, Production> entry : unknownProductions.entrySet()) {
            if(entry.getKey()==-1)
                baseProduction=entry.getValue();
            else{
                try {
                    int index=entry.getKey();
                    extraProductions.remove(index);
                    extraProductions.add(entry.getKey(),entry.getValue());
                }catch (NullPointerException e){
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    public ClientDeposits getDeposits() {
        return deposits;
    }

    public ClientFaithPath getFaithPath() {
        return faithPath;
    }

    /**
     *
     * @param index the index in the hand
     * @param ld the name of the card
     */
    public void putPlayedCard(Integer index,String ld){
        if(playedCards.get(index)!=null || discardedCards.get(index)!=null)
            playedCards.put(index+1,ld);
        else playedCards.put(index,ld);
    }

    public Map<Integer,String> getPlayedCards() { return playedCards; }

    /**
     *
     * @param index the index in the hand
     * @param ld the name of the card
     */
    public void putDiscardedCard(Integer index,String ld){
        if(discardedCards.get(index)!=null || playedCards.get(index)!=null)
            discardedCards.put(index+1,ld);
        else discardedCards.put(index,ld);
    }

    public void setDiscardedCards(Map<Integer, String> discardedCards) {
        this.discardedCards = discardedCards;
    }

    public void setPlayedCards(Map<Integer, String> playedCards) {
        this.playedCards = playedCards;
    }

    public Map<Integer,String> getDiscardedCards() { return discardedCards; }

    public Production getBaseProduction() {
        return baseProduction;
    }

    public List<Production> getActiveProductions() {
        return activeProductions;
    }

    public List<LeaderCard> getLeadersInBoard() {
        return leadersInBoard;
    }

    public List<LeaderCard> getLeadersInHand() {
        return leadersInHand;
    }

    public List<Production> getExtraProductions() {
        return extraProductions;
    }


    public Map<Integer, Stack<DevelopmentCard>> getSlots() {
        return slots;
    }

    public Integer getTotalCardsBought() {
        return totalCardsBought;
    }

    /**
     *
     * @return a Map of the active discounts
     */
    public Map<ResourceType,Integer> getActiveDiscounts() {
        Map<ResourceType,Integer> map = new HashMap<>();
        for (ResourceDiscount rd : discounts) {
            if(rd.isActivated())
                map.put(rd.getRes(),rd.getQuantity());
        }
        return map;
    }

    public void setActiveProductions(List<Production> activeProductions) {
        this.activeProductions = activeProductions;
    }

    public void setLeadersInHand(List<LeaderCard> leadersInHand) {
        this.leadersInHand = leadersInHand;
    }

    /**
     * reset all the production with unknown (base and extra)
     */
    public void resetProduction(){
        baseProduction.resetProduction();
        for(Production production : extraProductions) {
            production.resetProduction();
        }
    }

    /**
     * add an extra production
     * @param resourceType is the type of the extra production
     */
    public void addExtraProd (ResourceType resourceType) {
        Production production = new Production();
        try {
            production.addOutput(ResourceType.UNKNOWN,1);
            production.addInput(resourceType,1);
            production.addOutput(ResourceType.RED,1);
        } catch (IllegalResourceException e) {
            e.printStackTrace();
        }
        extraProductions.add(production);
    }

    /**
     * toggle the discount of the chosen type
     * @param res is the chosen resource type
     */
    public void toggleDiscount(ResourceType res) {
        for(ResourceDiscount discount : discounts){
            if(discount.getRes() == res) discount.toggle();
        }
    }

    /**
     * add a discount of the chose type
     * @param resourceType is the chose resource type
     */
    public void addDiscount(ResourceType resourceType) {
        ResourceDiscount resourceDiscount = new ResourceDiscount(resourceType);
        discounts.add(resourceDiscount);
    }

    /**
     *
     * @return the client board in a readable (ASCII ART) string
     */
    public String toString() {
        String stringBoard = faithPath.toString();
        stringBoard += deposits.toString();
        stringBoard += stringifySlots();
        stringBoard += stringifyLeaders();
        stringBoard += stringifyBaseProduction();
        return stringBoard;
    }

    /**
     * push a development card in a slot
     * @param slot is the slot id
     * @param card is the reference of the card
     */
    public void push(Integer slot, DevelopmentCard card) {
        if (slot != null && card != null && slots.get(slot) != null) {
            slots.get(slot).push(card);
            totalSlotPoints += card.getPoints();
            totalCardsBought++;
        }
    }

    /**
     *
     * @return a readable string of the active discounts
     */
    public String stringifyActiveDiscounts() {
        StringBuilder sb=new StringBuilder();
        sb.append(Color.ANSI_PURPLE.escape()).append("Active discounts: ").append(Color.RESET);
        for(ResourceDiscount rd: discounts){
            if(rd.isActivated()) sb.append("-").append(rd.getQuantity()).append(Utilities.resourceTypeToResource(rd.getRes()).label).append(" ");
        }
        return sb.toString();
    }

    /**
     *
     * @return a readable string of the productions
     */
    public String stringifyProductions() {
        StringBuilder sb=new StringBuilder();
        sb.append(stringifyBaseProduction());
        sb.append("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(Color.ANSI_PURPLE.escape()).append("EXTRA PRODUCTION: "+Color.RESET);
        for(Production p : extraProductions){
            sb.append("[").append(Utilities.stringify(p)).append("]");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     *
     * @return a readable string of the base production
     */
    public String stringifyBaseProduction() {
        return "\n═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n" +
                Color.ANSI_PURPLE.escape() + "BASE PRODUCTION: " + Color.RESET + Utilities.stringify(baseProduction) +
                "\n";
    }

    /**
     *
     * @return a redable string of the leaders (in hand and on board)
     */
    public String stringifyLeaders() {
        StringBuilder sb=new StringBuilder();
        sb.append("═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(Color.ANSI_PURPLE.escape()).append("LEADERS IN HAND: ").append(Color.RESET);
        for(LeaderCard ld : leadersInHand){
            sb.append(Utilities.stringify(ld));
            sb.append("\n                 ");
        }
        sb.append("\n");
        sb.append(Color.ANSI_PURPLE.escape()).append("LEADERS IN BOARD: ").append(Color.RESET);
        for(LeaderCard ld : leadersInBoard){
            sb.append(Utilities.stringify(ld));
            sb.append("\n                  ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     *
     * @return a readable string of the slots and stats
     */
    public String recapSlots() {
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<4;i++){
            sb.append(Color.ANSI_GREEN.escape()).append("LEVEL ").append(i).append(": ").append(Color.RESET);
            for(int j=1;j<4;j++){
                for(DevelopmentCard d : slots.get(j)){
                    if(d.getLevel() == i)
                        sb.append(Utilities.modelColorToClientColor(d.getColor()).escape()).append("■").append(Color.RESET);
                }
            }
            sb.append(" | ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     *
     * @return a readable string of the slots
     */
    public String stringifySlots(){
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<4;i++){
            sb.append(Color.ANSI_PURPLE.escape()).append("SLOT ").append(i).append(": ").append(Color.RESET);
            if(slots.get(i)!=null && slots.get(i).size()>0){
                sb.append(Utilities.stringify(slots.get(i).get(slots.get(i).size()-1)));
            }else sb.append("[ ]");
            sb.append("\n");
        }
        sb.append(Color.ANSI_PURPLE.escape()).append("TOTAL SLOT POINTS: ").append(Color.ANSI_YELLOW.escape()).append(totalSlotPoints).append(Color.RESET).append(" | ").append(Color.ANSI_PURPLE.escape()).append("TOTAL CARDS BOUGHT: ").append(Color.RESET).append(totalCardsBought).append("\n");
        sb.append(Color.ANSI_PURPLE.escape()).append("RECAP: ").append(Color.RESET).append(recapSlots());
        return sb.toString();
    }

    /**
     *
     * @return a readable string for pending list
     */
    public String stringifyPending(){
        StringBuilder mex = new StringBuilder();
        if(pendingResources.size() > 0) {
            mex.append("Deposit these pending resources:\n[");
            for (Resource resource : pendingResources) {
                String res=resource.toString().toLowerCase();
                mex.append(res).append("(").append(resource.label).append("),");
            }
            mex.deleteCharAt(mex.toString().length() - 1);
            mex.append("]");
        }
        return mex.toString();
    }

    public List<Resource> getPendingResources() {
        return pendingResources;
    }
}
