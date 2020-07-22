/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
