import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class DataCleaner {
	public static void main(String[] args) throws IOException {

		FileWriter sarWriter = new FileWriter("sars_clean/sar.txt", true);
		int outputCount = 1;
		int fileCount = 0;
		String line;
		Map<SarKey, Sar> sars = new HashMap<SarKey, Sar>();
		Map<SarKey, Sar> removable = new HashMap<SarKey, Sar>();
		File folder = new File("sars");
		File[] listOfFiles = folder.listFiles();
		System.out.println("list of files: " + Arrays.toString(listOfFiles));
		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (!file.getName().contains("DS_")) {
					// can't buffer billions of sars into memory
					BufferedReader br = new BufferedReader(new FileReader("sars/" + file.getName()));
					while ((line = br.readLine()) != null) {
						Sar s = new Sar(line);
						if (s.agent == 1 && s.reward == null)
							;
						else
							sars.put(new SarKey(s.parentId, s.choice, s.agent), s);
					}
					br.close();

				}
			}
			++fileCount;
			System.out.println("Done file: " + fileCount);
		}
		System.out.println("Done all files: " + fileCount);
		// clean
		for (Map.Entry<SarKey, Sar> s : sars.entrySet()) {
			Sar sar = s.getValue();
			// skip villain action and update hero's reward
			if (sar.agent == 1) {
				if (sar.reward != null) {
					SarKey collectorSar = new SarKey(sar.parentId, sar.choice, 0);
					sars.get(collectorSar).reward = -sar.reward;
				}
				removable.put(s.getKey(), sar);
			}
			// if (outputCount % 500 == 0)
				// System.out.println("Processing Sar # " + outputCount + ", entrySet size: " + sars.entrySet().size());
			outputCount++;
		}
		sars.keySet().removeAll(removable.keySet());
		removable.clear();
		for (Entry<SarKey, Sar> s : sars.entrySet()) {
			Sar sar = s.getValue();
			sar.reward = reward(sar, sars);
		} // sars has been cleaned
		for (Entry<SarKey, Sar> s : sars.entrySet()) { // remove incorrect choices
			Sar sar = s.getValue();
			UUID parent = sar.parentId;
			int agent = 0;

			// make arraylist of 3 rewards for each action
			int maxIndex = 0;
			for (int i = 1; i < 3; i++) {
				Sar old = sars.get(new SarKey(parent, maxIndex, agent));
				Sar compare = sars.get(new SarKey(parent, i, agent));
				if (old == null || compare == null|| compare.reward == null|| old.reward == null) {
//					System.out.println(old.toString() + ",\n" + compare.toString());
					continue;
				}
				// System.out.println("sars: " + old + ", " + compare);

				// if call and fold same, call.
				if (compare.reward > old.reward)
					maxIndex = i;
			}
			// highest index is skip
			// add all but skip to removable
			for (int i = 0; i < 3; i++) {
				if (i != maxIndex) {
					SarKey key = new SarKey(parent, i, agent);
					removable.put(key, sars.get(key));
				}
			}

		}
		sars.keySet().removeAll(removable.keySet());
		removable.clear();
		for (Entry<SarKey, Sar> s : sars.entrySet()) { // write cleaned and correct choices
			Sar sar = s.getValue();
			sarWriter.write(sar.tsar() + '\n');
			outputCount++;
		}
		sars.clear();

		sarWriter.close();
	}

	public static Integer reward(Sar sar, Map<SarKey, Sar> sars) {
		if (sar == null)
			return null;
		if (sar.reward != null)
			return sar.reward;
		// reward is null
		// get children from sars map
		// must have three children if reward is null.
//		System.out.println("parent : " + sar.toString());
		Sar c1 = sars.get(new SarKey(sar.stateId, 0, 0));
//		System.out.println("c1: " + c1.toString());
		Sar c2 = sars.get(new SarKey(sar.stateId, 1, 0));
//		System.out.println("c2: " + c2.toString());
		Sar c3 = sars.get(new SarKey(sar.stateId, 2, 0));
//		System.out.println("c3: " + c3.toString());
		if (c1 == null || c2 == null || c3 == null)
			return null; // oof
		int reward = Math.max(reward(c1, sars), Math.max(reward(c2, sars), reward(c3, sars)));

		return reward;
	}
}
