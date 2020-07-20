package com.mkflow.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 20/07/2020 18:00
 **/
public class Artifact {
	private List<String> files;

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public void addFile(String file) {
		if (files == null) {
			files = new ArrayList<>();
		}
		files.add(file);
	}
}
