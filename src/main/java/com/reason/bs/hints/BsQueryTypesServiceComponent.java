package com.reason.bs.hints;

import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.reason.Platform;
import com.reason.Streams;
import com.reason.ide.RmlNotification;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

// WARNING... THIS IS A BIG WIP...
public class BsQueryTypesServiceComponent implements BsQueryTypesService {

    private final Project m_project;
    private final VirtualFile m_baseDir;
    private final String m_bsbPath;

    public BsQueryTypesServiceComponent(Project project, VirtualFile baseDir, String bsbPath) {
        m_project = project;
        m_baseDir = baseDir;
        m_bsbPath = bsbPath;
    }

    @Override
    public InferredTypes types(VirtualFile file) {
        InferredTypesImplementation result = new InferredTypesImplementation();

        // Find corresponding cmi file... wip
        String filePath = file.getCanonicalPath();
        if (filePath != null) {
            String replace = Platform.removeProjectDir(m_project, filePath).replace(file.getPresentableName(), file.getNameWithoutExtension() + ".cmi");
            VirtualFile cmiFile = m_baseDir.findFileByRelativePath("lib/bs" + replace);
            if (cmiFile != null) {
                ProcessBuilder m_bscProcessBuilder = new ProcessBuilder(m_bsbPath, cmiFile.getCanonicalPath());
                String basePath = m_baseDir.getCanonicalPath();
                if (basePath != null) {
                    m_bscProcessBuilder.directory(new File(basePath));

                    Process bsc = null;
                    try {
                        bsc = m_bscProcessBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(bsc.getInputStream()));
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(bsc.getErrorStream()));

                        Streams.waitUntilReady(reader, errReader);
                        StringBuilder msgBuffer = new StringBuilder();
                        if (errReader.ready()) {
                            errReader.lines().forEach(line -> msgBuffer.append(line).append(System.lineSeparator()));
                            Notifications.Bus.notify(new RmlNotification("Code lens", msgBuffer.toString(), NotificationType.ERROR));
                        } else {
                            reader.lines().forEach(line -> {
                                //System.out.println("»" + line + "«");
                                if (line.startsWith("type") || line.startsWith("val") || line.startsWith("module") || line.startsWith("external")) {
                                    msgBuffer.append('\n');
                                }
                                msgBuffer.append(line);
                            });

                            String newText = msgBuffer.toString();
                            //System.out.println("---\n" + newText + "\n---");

                            String[] types = newText.split("\n");
                            for (String type : types) {
                                result.add(type);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // no ! nothing in fact
                    } finally {
                        if (bsc != null) {
                            bsc.destroy();
                        }
                    }
                }
            }
        }

        return result;
    }
}
