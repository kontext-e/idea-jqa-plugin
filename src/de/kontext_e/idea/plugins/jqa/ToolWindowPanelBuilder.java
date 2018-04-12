package de.kontext_e.idea.plugins.jqa;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.intellij.openapi.project.Project;

public class ToolWindowPanelBuilder {
    public static JPanel buildToolWindowPanel(final Project project) {
        JTextField tmpStoreTextField;
        JTextArea matchNCLASSReturnTextArea;
        JButton button1;

        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setAlignmentX(0.5f);
        final JLabel label1 = new JLabel();
        label1.setText("Database Location");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        tmpStoreTextField = new JTextField();
        tmpStoreTextField.setText("http://localhost:7474/db/data/");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(tmpStoreTextField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Query");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
//        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel1.add(label2, gbc);
        matchNCLASSReturnTextArea = new JTextArea();
        matchNCLASSReturnTextArea.setText("match (n:Class) return n LIMIT 10");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
		JScrollPane sp = new JScrollPane(matchNCLASSReturnTextArea);
		panel1.add(sp, gbc);
        button1 = new JButton();
        button1.setLabel("Find");
        button1.setText("Find");
        button1.addActionListener(new FindInNeo4jDatabaseAction(matchNCLASSReturnTextArea, tmpStoreTextField, project));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(button1, gbc);
        return panel1;
    }
}
