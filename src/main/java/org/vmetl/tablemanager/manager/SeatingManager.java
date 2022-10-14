package org.vmetl.tablemanager.manager;

import org.vmetl.tablemanager.model.CustomerGroup;
import org.vmetl.tablemanager.model.Table;

import java.util.*;
import java.util.function.BiConsumer;

import static org.vmetl.tablemanager.model.Table.TABLE_MAX_CAPACITY;

public class SeatingManager {

    // as we often remove customer groups from the middle of the list, linked list fits better.
    private final List<CustomerGroup> waitingCustomers = new LinkedList<>();
    private final Map<Integer, Set<Table>> availableTablesPerFreeSeats = new HashMap<>();
    private final Map<CustomerGroup, Table> groupTableMap = new HashMap<>();

    public SeatingManager(List<Table> tables) {

        initTablesMap(tables);

    }

    private void initTablesMap(List<Table> tables) {
        for (int i = 1; i <= TABLE_MAX_CAPACITY; i++) {
            availableTablesPerFreeSeats.put(i, new HashSet<>()); // change to TreeSet if we want 'smaller tables have higher priority' (this will slightly reduce speed)
        }

        for (Table table : tables) {
            availableTablesPerFreeSeats.get(table.getFreeSeats()).add(table);
        }
    }

    BiConsumer<CustomerGroup, Table.TableChangedEvent> groupPlacedListener = (group, changedTableEvent) -> {
        // rearrange the map according to the new table free sits number
        Table changedTable = changedTableEvent.getTable();

        int previousFreeSeats = changedTableEvent.getPreviousFreeSeats();
        if (previousFreeSeats != 0) {
            availableTablesPerFreeSeats.get(previousFreeSeats).remove(changedTable);
        }
        if (!changedTable.isFull()) {
            availableTablesPerFreeSeats.get(changedTable.getFreeSeats()).add(changedTable);
        }

        groupTableMap.put(group, changedTable);
    };

    BiConsumer<CustomerGroup, Table.TableChangedEvent> groupRemovedListener = (group, changedTableEvent) -> {
        Table changedTable = changedTableEvent.getTable();
        int previousFreeSeats = changedTableEvent.getPreviousFreeSeats();
        if (previousFreeSeats != 0) {
            availableTablesPerFreeSeats.get(previousFreeSeats).remove(changedTable);
        }
        availableTablesPerFreeSeats.get(changedTable.getFreeSeats()).add(changedTable);
        groupTableMap.remove(group);
    };


    /* Group arrives and wants to be seated.
     *
     * This method makes attempt to be O(1), so it's time does not change with the amount of Tables or Groups increasing.
     * This is achieved by following means:
     * - the map of free seats is maintained;
     * - all entities have unique ids, and it's guaranteed they do not collide in the hashmaps, making the retrieval from maps O(1)
     * */

    public void arrives(CustomerGroup group) {
        int groupSize = group.getSize();

        Optional<Table> bestTable = findBestTable(groupSize);

        if (bestTable.isPresent()) {
            Table table = bestTable.get();
            table.placeCustomerGroup(group, groupPlacedListener);
        } else {
            waitingCustomers.add(group);
        }
    }

    private Optional<Table> findBestTable(int groupSize) {
        Optional<Table> bestCandidate = Optional.empty();

        //- the scan starts from the bucket which contains tables with free seats equal or higher than the group size; empty buckets are ignored
        //- the scan returns early, just when it finds the best free table of the smallest size.

        for (int i = groupSize; i <= TABLE_MAX_CAPACITY; i++) {
            Set<Table> tablesWithRequiredFreeSeats = availableTablesPerFreeSeats.get(i);
            if (!tablesWithRequiredFreeSeats.isEmpty()) {
                return tablesWithRequiredFreeSeats.stream().findFirst();
            }
        }

        return bestCandidate;
    }

    /* Whether seated or not, the group leaves the restaurant.
    * This method works with O(n) complexity, where n is the number of waiting customer groups.
    * In the worst case, we have to go through all of them, trying to fit them in the new free space;
    * it's possible to fit waiting groups only in the newly freed table, no other tables are probed
    * (otherwise those customer groups wouldn't have been on the waiting list)
    * */
    public void leaves(CustomerGroup group) {
        Table freedTable = locate(group);
        freedTable.removeCustomerGroup(group, groupRemovedListener);

        Iterator<CustomerGroup> iterator = waitingCustomers.iterator();

        while (iterator.hasNext() && !freedTable.isFull()) {
            CustomerGroup waitingGroup = iterator.next();
            if (freedTable.getFreeSeats() >= waitingGroup.getSize()) {
                freedTable.placeCustomerGroup(waitingGroup, groupPlacedListener);
                iterator.remove();
            }
        }
    }

    /* Return the table at which the group is seated, or null if
     * they are not seated (whether they're waiting or already left).
     * The method works with O(1) complexity, because customer groups have unique ids and do not collide in the hashmap
     */
    public Table locate(CustomerGroup group) {
        return groupTableMap.get(group);
    }


    //------------ auxiliary methods (out of scope)


    public List<CustomerGroup> getWaitingCustomers() {
        return waitingCustomers;
    }

    public int getFreeSeats() {
        return groupTableMap.values().stream().mapToInt(Table::getFreeSeats).sum();
    }
}
