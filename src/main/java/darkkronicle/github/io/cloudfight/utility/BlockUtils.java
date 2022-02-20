package darkkronicle.github.io.cloudfight.utility;

import lombok.experimental.UtilityClass;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// https://stackoverflow.com/questions/41203118/iterate-over-all-connected-blocks-of-same-type/41426666
@UtilityClass
public class BlockUtils {

    private final BlockFace[] faces = {
            BlockFace.DOWN,
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private void getConnectedBlocks(Block block, Set<Block> results, List<Block> todo) {
        // Here I collect all blocks that are directly connected to variable 'block'.
        // (Shouldn't be more than 6, because a block has 6 sides)

        //Loop through all block faces (All 6 sides around the block)
        for (BlockFace face : faces) {
            Block b = block.getRelative(face);
            //Check if they're both of the same type
            if (b.getType() == block.getType()) {
                //Add the block if it wasn't added already
                if (results.add(b)) {

                    //Add this block to the list of blocks that are yet to be done.
                    todo.add(b);
                }
            }
        }
    }

    public Set<Block> getConnectedBlocks(Block block, int max) {
        Set<Block> set = new HashSet<>();
        LinkedList<Block> list = new LinkedList<>();

        // Add the current block to the list of blocks that are yet to be done
        list.add(block);

        // Execute this method for each block in the 'todo' list
        int current = 0;
        while ((block = list.poll()) != null && current <= max) {
            current++;
            getConnectedBlocks(block, set, list);
        }
        return set;
    }

}
