import java.util.UUID;

public final class SarKey {
	public final UUID parent;
	public final int choice;
	public final int agent;
	public SarKey(UUID parentId, int choice, int agent) {
		this.parent = parentId;
		this.choice = choice;
		this.agent = agent;
	}
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * parent.hashCode() + choice + agent;
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
//        	System.out.println("fail1\n");
        	return false;
        }
        if (getClass() != obj.getClass()) {
//        	System.out.println("fail2\n");
        	return false;
        }
        if (obj == this)
            return true;
        boolean equal = (parent.equals(((SarKey)obj).parent) && choice == ((SarKey)obj).choice && agent == ((SarKey)obj).agent);
//        System.out.println("equal value: "+ equal);
        return equal;
    }
}
