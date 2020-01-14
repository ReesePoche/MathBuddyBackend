package mbdbclient;

/**
 *A simple Data Structure  class composed of only public fields that contains all data
 *associated with an activity from the DataBase
 * relates to userData in that each userData object has a list of ACtivity Data
 * @author Reese
 */
public class ActivityData {
    public int ActID; //The ACtivityID of the given activity
    public int ActType; //The type of activity that this is. see MathBuddyDBSetup.sql for meaning
    public int numProbs; // The number of problems the given activy has
    public int classID; //The ID of the class that the activity is associated with
    public int score; //The score the given user has on the activity
    public int seed; //The seed of the score
}
