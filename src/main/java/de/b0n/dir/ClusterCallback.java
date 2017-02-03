package de.b0n.dir;


import java.util.Collection;
import java.util.Queue;

/**
 * Created by huluvu424242 on 03.02.17.
 */
public interface ClusterCallback<G, E> {

    public ClusterCallback<G, E> removeUniques();
    public void addGroupedElement(G group, E element);

}
