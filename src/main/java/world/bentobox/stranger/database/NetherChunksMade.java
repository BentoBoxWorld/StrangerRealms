package world.bentobox.stranger.database;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.Pair;

/**
 *  Stores chunks that have been made
 */
public class NetherChunksMade implements DataObject {
    
    @Expose
    private Set<Pair<Integer, Integer>> chunkSet = new HashSet<>();

    @Override
    public String getUniqueId() {
        return "NetherChunks";
    }

    @Override
    public void setUniqueId(String uniqueId) {
        //Nothing

    }

    /**
     * @return the chunkSet
     */
    public Set<Pair<Integer, Integer>> getChunkSet() {
        return chunkSet;
    }

    /**
     * @param chunkSet the chunkSet to set
     */
    public void setChunkSet(Set<Pair<Integer, Integer>> chunkSet) {
        this.chunkSet = chunkSet;
    }
    
    

}
