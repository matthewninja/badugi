import java.util.UUID;

class Sar { // State-action-reward vector
	UUID stateId, parentId;
	public int choice, drawsRemaining; 
	double handStrength; // unsure if matters given equity calculation
	public int pot, raises, toCall, oppDraws;
	int agent, position; // 0 = collector. 1 = other
	double fold_equity, call_equity, raise_equity;

	Sar(String sar) {
		String[] v = sar.split(",");
		stateId = UUID.fromString(v[0]);
		parentId = UUID.fromString(v[1]);
		choice = Integer.parseInt(v[2]);
		drawsRemaining = Integer.parseInt(v[3]);
		handStrength = Double.parseDouble(v[4]);
		pot = Integer.parseInt(v[5]);
		raises = Integer.parseInt(v[6]);
		toCall = Integer.parseInt(v[7]);
		oppDraws = Integer.parseInt(v[8]);
		// if (v[9].equals("null"))
		// 	reward = null;
		// else
		// 	reward = Integer.parseInt(v[9]);
		agent = Integer.parseInt(v[9]);
		position = Integer.parseInt(v[10]);
		handStrength = Double.parseDouble(v[11]);
		handStrength = Double.parseDouble(v[12]);
		handStrength = Double.parseDouble(v[13]);
	}

	Sar(UUID stateId, UUID parentId, int choice, int drawsRemaining, double handStrength, int pot, int raises, int toCall,
			int oppDraws, int agent, int position, double fold_equity, double call_equity, double raise_equity) {
		this.stateId = stateId;
		this.parentId = parentId;
		this.choice = choice;
		this.drawsRemaining = drawsRemaining;
		this.handStrength = handStrength;
		this.pot = pot;
		this.raises = raises;
		this.toCall = toCall;
		this.oppDraws = oppDraws;
		this.agent = agent;
		this.position = position;
		this.fold_equity = fold_equity;
		this.call_equity = call_equity;
		this.raise_equity = raise_equity;
	}

	@Override
	public String toString() {
		return stateId.toString() + ',' + parentId.toString() + ',' + choice + ',' + drawsRemaining + ',' + handStrength
				+ ',' + pot + ',' + raises + ',' + toCall + ',' + oppDraws + ',' + agent+','+position
				+','+fold_equity+','+call_equity+','+raise_equity;
	}

	public String tsar() { // training state-action-reward vector
		if (choice == 0) {
			return "" + drawsRemaining + ',' + handStrength + ',' + pot + ',' + raises + ',' + toCall + ',' + oppDraws + ','
					+ position+",1,0,0";
		} else if (choice == 1) {
			return "" + drawsRemaining + ',' + handStrength + ',' + pot + ',' + raises + ',' + toCall + ',' + oppDraws + ','
					+ position+",0,1,0";
		} else
		return "" + drawsRemaining + ',' + handStrength + ',' + pot + ',' + raises + ',' + toCall + ',' + oppDraws + ','
				+ position+",0,0,1";
	}
}