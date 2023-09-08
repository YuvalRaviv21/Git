import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/*|-----------------Important Notes--------------------|
  	In order to make wit work properly, you will need to download
  	and build CLASSPATH to Apache Commons IO:
 	https://commons.apache.org/proper/commons-io/download_io.cgi*/
public class wit {
	public static void main(String[] args) throws IOException {
		String cwd = System.getProperty("user.dir");
		String ssd = cwd.substring(0, 2);
		File witedDir = rootedWitParent(cwd);
		if(args.length!=0) {
		
			if (args[0].toLowerCase().equals("init")) {
				
				init(cwd);}
			else if (args[0].toLowerCase().equals("add")) {
				File path2 = new File(args[1]);
				if(path2.exists()) {
					if (args[1].startsWith(ssd)) {
						add(args[1]);
					}
					else
						add(cwd+"\\"+args[1]);
					}
			}
			//
			else if (witedDir==null){
				throw new IllegalAccessError("no dir with .wit found.");
			}
			else if(!(witedDir==null)){
				if (args[0].toLowerCase().equals("commit")) {
					commit(args[1], witedDir);
				}
				else if (args[0].toLowerCase().equals("status")) {
					List<Collection<File>> statusList = status(witedDir);
					@SuppressWarnings("deprecation")
					List<String> references = FileUtils.readLines(new File(witedDir+"/.wit/references.txt"));
					System.out.println(references.get(0).substring(6)+
							"\n______________________________\nChanges to be committed:");
					print.Collection(statusList.get(0));
					System.out.println("\n______________________________\n\nChanges not staged for commit:");
					print.Collection(statusList.get(1));
					System.out.println("\n______________________________\n\nUntracked files:");
							// not including .matadata files, to include, replace untracked(filesInDir)) with filesInDir;
					print.Collection(notIncludeFiles((Collection<File>)(statusList.get(2)),new String[]{"\\.metadata"}));
					System.out.println("\n______________________________");
				}
				else if (args[0].toLowerCase().equals("checkout")&& !args[1].isEmpty()) {
					System.out.println("here");
					checkout(witedDir,args[1]);
				}

			}
		}
		System.out.println("arafa");
	}
	@SuppressWarnings("deprecation")
	private static void checkout(File witedDir, String commit_id) throws IOException {
		List<Collection<File>> statusList = status(witedDir);
		if(statusList.get(0).isEmpty()&& statusList.get(1).isEmpty()) {
			File commit_id_File;
			File staging_areaFile = new File(witedDir+"/.wit/staging_area");
			File referencesFile = new File(witedDir+"/.wit/references.txt");
			List<String> referencesText = FileUtils.readLines(referencesFile );
			if((commit_id.toLowerCase()).equals("master")) {
				String master = referencesText.get(1).substring(8);
				commit_id_File = new File(witedDir+"/.wit/images/"+master);;
			}
			else {
				commit_id_File = new File(witedDir+"/.wit/images/"+commit_id);
				if(!commit_id_File.exists())
					throw new IllegalAccessError("commit_id does not exist");
			}
			FileUtils.copyDirectory(commit_id_File, witedDir);
			FileUtils.copyDirectory(commit_id_File, staging_areaFile);
			writeToFile(referencesFile,"HEAD= " +commit_id_File.getName()+"\n"
									+referencesText.get(1));
		}
		else {
			System.out.println("fail");
		}
	}

	@SuppressWarnings("deprecation")
	private static List<Collection<File>> status(File witedDir) throws IOException {
		Collection<File> filesInDir = statusFiles(witedDir);
		File staging_area = new File(witedDir+"/.wit/staging_area");
		Collection<File> staging_areaDir =FileUtils.listFiles(staging_area,
				TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		Collection<File> file_not_stage_fot_commit = new HashSet<>();
		Collection<File> toAddFiles = new LinkedList<>();
		for( String a: new LinkedList<>(FileUtils.readLines( new File(witedDir.toString()+"/.wit/added_files_to_commit.txt"))))
			toAddFiles.add(new File(a));
		for(File staging_areaFile: staging_areaDir) {
			Path relative = staging_area.toPath().relativize(staging_areaFile.toPath());
			File originalFile = new File(witedDir+"\\"+relative.toString());
			if(filesInDir.remove(originalFile)&& !FileUtils.contentEquals(originalFile, staging_areaFile))
				file_not_stage_fot_commit.add(staging_areaFile);
		}
		List<Collection<File>> answer = new LinkedList<>();
		answer.add(toAddFiles);
		answer.add(file_not_stage_fot_commit);
		answer.add(filesInDir);
		return answer;
	}
//	@SuppressWarnings("deprecation")
//	public static void status(String cwd) throws IOException {
//		File witedDir = rootedWitParent(cwd);
//		Collection<File> filesInDir = statusFiles(witedDir);
//		File staging_area = new File(witedDir+"/.wit/staging_area");
//		Collection<File> staging_areaDir =FileUtils.listFiles(staging_area,
//				TrueFileFilter.INSTANCE,
//				TrueFileFilter.INSTANCE);
//		Collection<File> file_not_stage_fot_commit = new HashSet<>();
//		Collection<String> toAddFiles = new LinkedList<>(FileUtils.readLines( new File(witedDir.toString()+"/.wit/added_files_to_commit.txt")));
//		for(File staging_areaFile: staging_areaDir) {
//			Path relative = staging_area.toPath().relativize(staging_areaFile.toPath());
//			File originalFile = new File(witedDir+"\\"+relative.toString());
//			if(filesInDir.remove(originalFile)&& !FileUtils.contentEquals(originalFile, staging_areaFile))
//				file_not_stage_fot_commit.add(staging_areaFile);
//		}
//		List<String> references = FileUtils.readLines(new File(witedDir+"/.wit/references.txt"));
//		System.out.println(references.get(0).substring(6));
//		System.out.println("______________________________\nChanges to be committed:\n\n"
//						+toAddFiles
//						+"\n______________________________\n\nChanges not staged for commit:\n\n"
//						+file_not_stage_fot_commit
//						+"\n______________________________\n\nUntracked files:\n\n"
//						// not including .matadata files, to include, replace untracked(filesInDir)) with filesInDir;
//						+ untracked(filesInDir));
//	}
	
	public static Collection<File> notIncludeFiles(Collection<File> dir,String[] contains) {
		Collection<File> answer = new HashSet<File>();
		for(File f: dir)
			for(String t: contains)
			if(!(f.toString().contains(t)))
				answer.add(f);
		return answer;
	}
	
//	@SuppressWarnings("deprecation")
//	public static void status(String cwd) throws IOException {
//		File cwdF = new File(cwd);
//		File witDirFile = rootedWitParent(cwd);
//		File staging_area = new File(witDirFile+"/.wit/staging_area");
//		Collection<File> cwdFiles =FileUtils.listFiles(cwdF,
//				TrueFileFilter.INSTANCE,
//				TrueFileFilter.INSTANCE);
//		Collection<File> staging_areaFiles =FileUtils.listFiles(staging_area,
//				TrueFileFilter.INSTANCE,
//				TrueFileFilter.INSTANCE);
//		System.out.println(cwdF);
//		System.out.println(witDirFile);
//		System.out.println(cwdFiles.remove(new File("C:\\Users\\yuval\\Desktop\\Stuff\\Where are my files.lnk")));
//		print.Collection(cwdFiles);
//		print.Collection(staging_areaFiles);
//		for( File tFile : staging_areaFiles) {
//			System.out.println("__");
//			System.out.println(staging_area.toPath().relativize(tFile.toPath()));
//			System.out.println(tFile);
//			File bFile = new File(witDirFile+"\\"+staging_area.toPath().relativize(tFile.toPath()).toString());
//			System.out.println(bFile);
//			System.out.println(FileUtils.isFileOlder(tFile, bFile));}
//	}
	
	public static void init(String cwd) throws IOException {
		Folder.Create(cwd, ".wit");
		String pathOfDotwit = cwd + "\\.wit";
		Folder.Create(pathOfDotwit, "images");
		Folder.Create(pathOfDotwit, "staging_area");
		File referFile = new File(pathOfDotwit+"/added_files_to_commit.txt");
		writeToFile(referFile, "");
		System.out.println("init opperation is finished;");
		System.out.println("\t\t"+pathOfDotwit);
		writeToFile(pathOfDotwit+"/references.txt", "Head= null\n"
					+"master= null");
		
	}
	public static void add(String path2) throws IOException {
		/*
		*first section is the part where we find node in path to the root that
		*contains directory .wit;
		*/
		File source = new File(path2);
		File target =  rootedWitParent(path2);
		File aftcf = new File(target+"\\.wit\\added_files_to_commit.txt");
		// duplicate each directory/file on the path;
		if (target != null) {
			Path targetFilePath = Paths.get(target.getPath());
			Path sourceFilePath = Paths.get(source.getPath());
			Path relativePath = targetFilePath.relativize(sourceFilePath); // which directories to copy.
			Iterator<Path> pathIterator = relativePath.iterator();
			sourceFilePath = targetFilePath;
			targetFilePath = Paths.get(targetFilePath + "/.wit/staging_area");
			Path name = null;
			while (pathIterator.hasNext()) {
				name = pathIterator.next();
				targetFilePath = Paths.get(targetFilePath + "/" + name);
				sourceFilePath = Paths.get(sourceFilePath + "/" + name);
				try {
					FileUtils.copyFile(sourceFilePath.toFile(), targetFilePath.toFile());
					System.out.println(sourceFilePath);
					wtaftc(aftcf,sourceFilePath);
				} catch (Exception e) {
				} finally {}
				// if add() received a directory to add, it will copy all the files in it;
				if (!pathIterator.hasNext() && name.toFile().isDirectory()) {
					source = new File(sourceFilePath.toString());
					target = new File(targetFilePath.toString());
					FileUtils.copyDirectory(source, target);
					wtaftc(aftcf,source.toPath());
				}
			}
		} else {
			throw new IllegalCallerException("No .wit folder found");
		}
		System.out.println("add Action run.");
	}
	public static void commit(String MESSAGE,File witParenetFile) throws IOException {
		if(witParenetFile != null) {
			Path images = Paths.get(witParenetFile+"/.wit/images");
			Path staging_area = Paths.get(witParenetFile+"/.wit/staging_area");
			Path id = Paths.get(witParenetFile+"/.wit/images/"+hexFileName(40));
			Folder.Create(images, id.getFileName());
			File referFile = new File(witParenetFile+"/.wit/references.txt");
			File aftcf = new File(witParenetFile+"/.wit/added_files_to_commit.txt");
			String parent;
			try {
				BufferedReader refeReader = new BufferedReader(new FileReader(referFile));
	        	parent = refeReader.readLine().substring(6);
	        	refeReader.close();
			} catch (Exception e) {
				parent = "None";
			}
			writeToFile(id+".txt", "parent= " +parent+"\n"
					+"date= " + getDateTimeStamp()
					+"\nmessage=" + MESSAGE);
			FileUtils.copyDirectory(staging_area.toFile(), id.toFile());
			writeToFile(referFile, "HEAD= "+id.getFileName()
			+"\nmaster= "+id.getFileName());
			writeToFile(aftcf,"");
		}
		else {
			System.out.println("Error");
		}
	}
	
	/*
	 * Functions dedicated to help organize wit class;
	 */
	
	//write to added_files_to_commit
	@SuppressWarnings({ "deprecation"})
	private static void wtaftc (File aftcf,Path file) throws IOException {
		String[] t = null;
		HashSet<File> aSet = new HashSet<>( FileUtils.streamFiles(new File(file.toString()), true,t).toList());
		Set<String> linesINaftc = new HashSet<String>(FileUtils.readLines(aftcf));
//		System.out.println("____________________________________________________________");
//		System.out.println(linesINaftc);
//		System.out.println("***********************_________________*******************");
//		System.out.println(file);
//		System.out.println(aftcf);
		for(File e : aSet)
			linesINaftc.add(e.toString());
//		System.out.println(linesINaftc);
		FileWriter aftc = new FileWriter(aftcf.toString());	
		for( Object f : linesINaftc)
			aftc.write(f.toString()+"\n");
		aftc.close();
	}
	
	
	private static void writeToFile(Object referFile,String massege ) throws IOException {
		FileWriter references = new FileWriter(referFile.toString());
		references.write(massege);
		references.close();
	}
	private static String getDateTimeStamp() {
		Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z ",new Locale("en"));
        return dateFormat.format(currentDate);
	}
	private static String hexFileName(int length) {
		String nameString ="";
		String hexChar[]
		        = { "0", "1", "2", "3", "4", "5",
		            "6", "7", "8", "9", "a", "b",
		            "c", "d", "e", "f" };
		for (int i = 0; i < length; i++) {
			nameString = nameString + hexChar[(int)(Math.random()*16)];
		}
		return nameString;
	}	
	
	/*give all file in dir except ones in .wit dir
	 * (possible to make output be file and dirs);
	 */
	public static Collection<File> statusFiles(File dir) throws IOException {
		if(!new File(dir+"/.wit").exists())
			throw new IOException("no .wit directory found in input file.");
		return statusFiles(new TreeSet<>(),new TreeSet<>(), dir);
	}
	private static Collection<File> statusFiles(Collection<File> files,Collection<File> dirs,File dir) {
		File[] files2 = dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return !(dir.toString()+name).contains(".wit");
			}
		});
		for(File f: files2) {
			if(f.isDirectory()) {
				dirs.add(f);
				statusFiles(files,dirs, f);
			}
		}
		Collections.addAll(files, files2);
		files.removeAll(dirs);
		return files;
	}
	private static File rootedWitParent(String path) {
		/*
		*return the closest dir to path that contains wit. dir.
		*/
		File source = new File(path);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File source, String name) {
				return name.startsWith(".wit");
			}};
		File target = source.getParentFile();
		while (target != null) {
			String[] contain = target.list(filter);
			if (contain != null && contain.length > 0) {
				break;
			}
			target = target.getParentFile();
		}
		return target;
	}
	private class Folder{
		public static void Create(String path, String name) {
			File f1 = new File(path + "\\" + name);
			boolean bool = f1.mkdir();
			if (bool) {
				System.out.println("Folder ["+name+"] was created successfully in "+f1.getParent());
			} else {
				System.out.println("Error Found!");
			}
		}
		public static void Create(File path, String name) {
			Create(path.toString(), name);
		}
		public static void Create(Path path, String name) {
			Create(path.toFile(), name);
		}
		public static void Create(Path path, Path name) {
			Create(path.toFile(), name.toString());
		}
	}
	private class print<E>{
		public static void Array(Object[] arr2) {
	    	if(arr2 !=null ) {
		    	int size = arr2.length;
		    	String ans = "[";
		    	for(int i = 0; i < size; i++)
		    		ans = ans + arr2[i].toString()+" \n";
		    	System.out.println(ans.substring(0,ans.length()-2)+"]");
	    	}
	    }
		public static void List(List<String> arr) {
	    	if(arr !=null ) {
	    		int size = arr.size();
		    	String ans = "[";
		    	for(int i = 0; i < size; i++)
		    		ans = ans + arr.get(i).toString()+" \n";
		    	System.out.println(ans.substring(0,ans.length()-2)+"]");
	    	}
	    }
		public static void Collection(Collection<File> arr) {
	    	if(arr !=null ) {
	    		System.out.println("[");
	    		for(File t : arr)
	    			System.out.println(t.toString());
	    		System.out.print("]");
			}
	    }
	}
}
