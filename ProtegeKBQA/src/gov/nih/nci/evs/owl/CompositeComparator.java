package gov.nih.nci.evs.owl;

import java.util.Comparator;

@SuppressWarnings({ "unchecked", "rawtypes" })
class CompositeComparator implements Comparator {
    private final Comparator major;
    private final Comparator minor;

    public CompositeComparator(Comparator major, Comparator minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public int compare(Object o1, Object o2) {
        final int result = this.major.compare(o1, o2);
        if (result != 0) {
            return result;
        } else {
            return this.minor.compare(o1, o2);
        }
    }
}
