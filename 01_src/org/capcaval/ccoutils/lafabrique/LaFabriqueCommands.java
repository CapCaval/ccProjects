package org.capcaval.ccoutils.lafabrique;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.capcaval.ccoutils.commandline.Command;
import org.capcaval.ccoutils.commandline.CommandParam;
import org.capcaval.ccoutils.file.FileTool;
import org.capcaval.ccoutils.file.FileTool.FilePropertyEnum;
import org.capcaval.ccoutils.lafabrique.command.CommandBuild;
import org.capcaval.ccoutils.lafabrique.command.CommandCompile;
import org.capcaval.ccoutils.lafabrique.command.CommandEclipseProject;
import org.capcaval.ccoutils.lafabrique.command.CommandJar;
import org.capcaval.ccoutils.lafabrique.command.CommandResult;
import org.capcaval.ccoutils.lang.ArrayTools;
import org.capcaval.ccoutils.lang.StringMultiLine;
import org.capcaval.ccoutils.compiler.Compiler;


public class LaFabriqueCommands {

	@Command
	public String updateEclipseProject(String projectStr) {
		StringMultiLine returnedMessage = new StringMultiLine("[laFabrique] INFO  : update eclipse " + projectStr  + " project.");
		// by default do it one the current directory
		returnedMessage.addLine(this.updateEclipseProject(Paths.get(".")));
		
		return returnedMessage.toString();
	}

	public String updateEclipseProject(Path path) {
		
		
		
		// first compile the project
		AbstractProject project = this.compileProject(path);
		CommandResult.applicationName = "laFab";
		CommandResult cr = CommandEclipseProject.updateEclipseProject(project);
		
		return cr.getMessage();
	}

	
	@Command
	public String newProject(
			@CommandParam(name="project name", desc="Define the name of the project.")
			String name, 
			@CommandParam(name="project path", desc="Define the path of the project.")
			String pathStr) {
		// compile the local project
		AbstractProject rootProj = compileProject(Paths.get("."));

		Path projectPath = Paths.get(pathStr, name);
		
		// clean the directory
		try {
			if (projectPath.toFile().exists() == true) {
				FileTool.deleteFile(projectPath);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<Path> subDirList = ArrayTools.newArrayList(rootProj.projectDir);
		// add all the source directory
		for(Path srcPath : rootProj.sourceList){
			subDirList.add(srcPath);
		}
		// add all the lib directory
		for(Path libPath : rootProj.libDirList){
			subDirList.add(libPath);
		}
		
		StringMultiLine returnedMessage = new StringMultiLine("[laFabrique] INFO : create all following sub-dirs at "
				+ projectPath.toString());

		try {
			for (Path dir : subDirList) {
				Path p = Paths.get(projectPath.toString(), dir.toString());

				if (p.toFile().exists() == true) {
					System.out.print("Directory " + p + " already exist, do you want to overwrite it ? ");
					String str = System.console().readLine();
					if (str.contains("y")) {
						Files.createDirectories(p);
					}
				} else {
					Files.createDirectories(p);
					returnedMessage.addLine("[laFabrique] INFO : " + p + " is created");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			returnedMessage.addLine("[laFabrique] ERROR : can not create the directory : " + projectPath.toString());
		}

		try {
			returnedMessage.addLine(this.getClass().getResource("laFab").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// copy the ccoutils lib
		// this.copyFile( returnedMessage,
		// this.getClass().getResource("laFab").getFile(),
		// projectPath.toString());
		// to do verify that the jar is existing otherwise get it inside 20_prod
		if (Files.exists(Paths.get("02_lib/C3.jar"))) {
			this.copyFile(returnedMessage, "02_lib/C3.jar", projectPath.toString() + "/02_lib/C3.jar");
		} else if (Files.exists(Paths.get("20_prod/C3.jar"))) {
			this.copyFile(returnedMessage, "20_prod/C3.jar", projectPath.toString() + "/02_lib/C3.jar");
		} else {
			returnedMessage
					.addLine("[laFabrique] ERROR : can not find C3.jar inside 02_lib or inside 20_prod directories.");
		}
		// copy JUnit
		if (Files.exists(Paths.get("02_lib/junit-4.8.2.jar"))) {
			this.copyFile(returnedMessage, "02_lib/junit-4.8.2.jar", projectPath.toString() + "/02_lib/junit-4.8.2.jar");
		} else {
			returnedMessage
					.addLine("[laFabrique] ERROR : can not find junit-4.8.2.jar inside 02_lib.");
		}

		
		// copy the linux script
		// this.copyFile( returnedMessage,
		// this.getClass().getResource("laFab").getPath(),
		// projectPath.toString());
		// // copy the windows script
		// this.copyFile( returnedMessage,
		// this.getClass().getResource("laFab.bat").getPath(),
		// projectPath.toString());

		InputStream laFabStream = this.getClass().getResourceAsStream("laFab");
		FileTool.saveInputStream(laFabStream, Paths.get(projectPath.toString(), "laFab"), FilePropertyEnum.xr_);

		InputStream laFabBatStream = this.getClass().getResourceAsStream("laFab.bat");
		FileTool.saveInputStream(laFabBatStream, Paths.get(projectPath.toString(), "laFab.bat"), FilePropertyEnum.xr_);

		// create a new Proj
		try {
			// Path templatePath =
			// Paths.get(this.getClass().getResource("Project.java.template").getPath());
			InputStream prjStream = this.getClass().getResourceAsStream("Project.java.template");

			InputStreamReader prjReader = new InputStreamReader(prjStream);

			// Path projPath = Paths.get(projectPath.toString(), "00_project");
			// Files.createDirectories(projPath);

			FileTool.replaceTokenInFile(prjReader, Paths.get(projectPath.toString() + "/00_prj/prj/", "Project.java"),
					ArrayTools.newMap("projectName", name), '{', '}');
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		AbstractProject proj = compileProject(projectPath);
		
		CommandEclipseProject.updateEclipseProject(proj);
		
		return returnedMessage.toString();
	}

	protected AbstractProject compileProject(Path path) {
		AbstractProject proj=null;
		try {
			proj = Compiler.compile(AbstractProject.class, path.toString() + "/00_prj", "prj/Project.java", "./10_bin");
			// set the current path of the project
			proj.rootProjectDirPath = path.toAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proj;
	}

	public void copyFile(StringMultiLine returnedMessage, String srcFile, String destDir) {
		try {
			// copy the file
			Files.copy(Paths.get(srcFile), Paths.get(destDir), StandardCopyOption.REPLACE_EXISTING);
			returnedMessage.addLine("[laFabrique] INFO : " + Paths.get(destDir + "/" + srcFile).toFile().toString()
					+ " is added");

		} catch (IOException e) {
			returnedMessage.addLine("[laFabrique] ERROR : can not copy the file : " + Paths.get(srcFile) + " to "
					+ Paths.get(destDir + "/" + srcFile));
			returnedMessage.addLine(e.toString());
			if (e.getCause() != null) {
				returnedMessage.addLine(e.getCause().toString());
			}
		}
	}

	@Command(desc="build project")
	public String build(
			@CommandParam(name="project name", desc="Name of the project to be built.")
			String projectStr) {
		StringMultiLine returnedMessage = new StringMultiLine("[laFabrique] INFO  : Build start " + projectStr  + " project.");

		// compile the project first
		AbstractProject proj = this.retrieveProject(projectStr);

		if(proj == null){
			returnedMessage.addLine("[laFabrique] ERROR : Error project " + projectStr + " can not be found.");
			returnedMessage.addLine("                     the file 00_prj/prj/" + projectStr + ".java can not be found.");
			return returnedMessage.toString();
		}
		
		try {
			returnedMessage.addLine("[laFabrique] INFO  : Compile all classes");
			// Now that the project is there let's build it
			CommandResult cr = CommandCompile.compile(proj);
			returnedMessage.addLine( cr.getMessage());

			if (proj.jar.name != null) {
				returnedMessage.addLine("[laFabrique] INFO  : Build the jar");
				cr = CommandJar.makeJar(proj);
				returnedMessage.addLine(cr.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnedMessage.toString();
	}
	@Command(desc="pack project")
	public String pack(
			@CommandParam(name="project name", desc="Name of the project to be packed.")
			String projectStr
			){
		StringMultiLine returnedMessage = new StringMultiLine("[laFabrique] INFO  : Build start " + projectStr  + " project.");
		
		
		return returnedMessage.toString();
	}
	
	
	public AbstractProject retrieveProject(String projectName){
		String filePath = "prj/" + projectName + ".java";
		
		// if the project do not exist return null
		if(FileTool.isFileExist("00_prj/" + filePath) == false){
			return null;
		}
		
		// compile the project first
		AbstractProject proj = null;
		try {
			proj = Compiler.compile(AbstractProject.class, "00_prj", filePath, "10_bin");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return proj;
	}

}
