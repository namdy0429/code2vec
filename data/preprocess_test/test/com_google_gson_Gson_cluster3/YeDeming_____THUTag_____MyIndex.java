import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyIndex {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String targetFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String indexFile = "/home/cxx/smt/index";
			String outputFile1 = "/home/cxx/smt/bookTagExample";
			String outputFile4 = "/home/cxx/smt/bookExample";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
			BufferedWriter outBook = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile1), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			BufferedWriter outIndex = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(indexFile), "UTF-8"));
			HashSet<Integer> sourceIndex = new HashSet<Integer>();
			HashSet<Integer> targetIndex = new HashSet<Integer>();
			HashSet<Integer> index = new HashSet<Integer>();
			String sLine, tLine;
			while ((sLine = source.readLine()) != null) {
				sourceIndex.add(g.fromJson(sLine, Doc.class).id);
			}
			while ((tLine = target.readLine()) != null) {
				targetIndex.add(g.fromJson(tLine, TargetDoc.class).subject_id);
			}
			Iterator it = sourceIndex.iterator();
			while (it.hasNext()) {
				int i = (Integer) it.next();
				if (targetIndex.contains(i)) {
					index.add(i);
					outIndex.write(i);
					outIndex.newLine();
					outIndex.flush();
				}
			}
			outIndex.close();
			source.close();
			target.close();
			source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));

			String targetLine;
			boolean firstBook = true;
			int preId = 1001;
			int curId = 1001;
			int counter = 0;
			while ((targetLine = target.readLine()) != null) {
				TargetDoc tDoc = g.fromJson(targetLine, TargetDoc.class);
				switch (tDoc.cat_id) {
				case 1001:
					if (index.contains(tDoc.subject_id)) {
						if (firstBook) {
							firstBook = false;
							outBook.write(tDoc.tag);
							preId = curId = tDoc.subject_id;
							counter = 1;
							continue;
						}
						curId = tDoc.subject_id;
						if (curId == preId) {
							outBook.write(" " + tDoc.tag);
							preId = curId;
						} else {
							outBook.newLine();
							outBook.flush();
							outBook.write(tDoc.tag);
							preId = curId;
							counter++;
						}
					}
					break;
				default:
				}
				if (counter >= 5000)
					break;
			}
			outBook.newLine();
			outBook.flush();
			outBook.close();
			target.close();
			outBook = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile4), "UTF-8"));
			WordSegment ws = new ForwardMaxWordSegment();
			String sourceLine;
			counter = 0;
			while ((sourceLine = source.readLine()) != null) {
				Doc doc = g.fromJson(sourceLine, Doc.class);
				String content = doc.title + "," + doc.description;
				String reg = "[\n-\r]";
				Pattern p = Pattern.compile(reg);
				Matcher m = p.matcher(content);
				String newContent = m.replaceAll("");
				String[] words = ws.segment(newContent);
				switch (doc.cat_id) {
				case 1001:
					if (index.contains(doc.id)) {
						counter++;
						for (int i = 0; i < words.length - 1; i++) {
							outBook.write(words[i] + " ");
						}
						outBook.write(words[words.length - 1]);
						outBook.newLine();
						outBook.flush();
					}
					break;
				default:
				}
				if (counter >= 5000)
					break;
			}
			source.close();
			outBook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
