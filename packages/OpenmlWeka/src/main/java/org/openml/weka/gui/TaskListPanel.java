/*
BSD 3-Clause License

Copyright (c) 2017, Jan N. van Rijn <j.n.van.rijn@liacs.leidenuniv.nl>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

package org.openml.weka.gui;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;

import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.Task;
import org.openml.weka.experiment.TaskBasedExperiment;

import weka.experiment.Experiment;
import weka.gui.JListHelper;
import weka.gui.experiment.DatasetListPanel;

public class TaskListPanel extends DatasetListPanel {

	private static final long serialVersionUID = 1L;

	/** The experiment to set the dataset or task list of. */
	protected Experiment m_Exp;
	
	private OpenmlConnector apiconnector;

	public TaskListPanel( OpenmlConnector ac) {
		super();
		this.apiconnector = ac;
	    setBorder(BorderFactory.createTitledBorder("Tasks"));
	}

	/**
	 * sets the state of the buttons according to the selection state of the
	 * JList.
	 * 
	 * @param e
	 *            the event
	 */
	private void setButtons(ListSelectionEvent e) {
		if ((e == null) || (e.getSource() == m_List)) {
			m_DeleteBut.setEnabled(m_List.getSelectedIndex() > -1);
			m_EditBut.setEnabled(false);
			m_UpBut.setEnabled(JListHelper.canMoveUp(m_List));
			m_DownBut.setEnabled(JListHelper.canMoveDown(m_List));
		}
	}

	/**
	 * Tells the panel to act on a new experiment.
	 * 
	 * @param exp
	 *            a value of type 'TaskBasedExperiment'
	 */
	@Override
	public void setExperiment(Experiment exp) {
		m_Exp = exp;
		m_List.setModel(getTasksControlled(m_Exp));
		m_AddBut.setEnabled(true);
		setButtons(null);
	}
	
	/**
	 * Handle actions when buttons get pressed.
	 * 
	 * @param e
	 *            a value of type 'ActionEvent'
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_AddBut) {
			actionPerformedTaskBasedAdd(e);
		} else if (e.getSource() == m_DeleteBut) {
			// Delete the selected files
			int[] selected = m_List.getSelectedIndices();
			if (selected != null) {
				for (int i = selected.length - 1; i >= 0; i--) {
					int current = selected[i];
					getTasksControlled(m_Exp).removeElementAt(current);
					if (getTasksControlled(m_Exp).size() > current) {
						m_List.setSelectedIndex(current);
					} else {
						m_List.setSelectedIndex(current - 1);
					}
				}
			}
			setButtons(null);
		} else if (e.getSource() == m_EditBut) {
			actionPerformedTaskBasedEdit(e);
		} else if (e.getSource() == m_UpBut) {
			JListHelper.moveUp(m_List);
		} else if (e.getSource() == m_DownBut) {
			JListHelper.moveDown(m_List);
		}
	}

	public void actionPerformedTaskBasedAdd(ActionEvent e) {
		String s = (String) JOptionPane.showInputDialog(this,
				"A comma-separated list of the task id's from OpenML.org:",
				"OpenML Task id's", JOptionPane.PLAIN_MESSAGE);
		try {
			int[] input_task_ids = Conversion.commaSeparatedStringToIntArray(s);
			for (int i = 0; i < input_task_ids.length; ++i) {
				if (getTasksControlled(m_Exp).contains(new Task(input_task_ids[i])) == false) {
					try {
						Task t = apiconnector.taskGet(input_task_ids[i]);
						// download all data necessary for task execution

						if( m_Exp instanceof TaskBasedExperiment ) {
							getTasksControlled(m_Exp).addElement(t);
						} else {
							System.err.println("Could not add task to Queue... ");
						}
					} catch (Exception downloadException) {
						downloadException.printStackTrace();
						JOptionPane.showMessageDialog(
							this,
							"There occured an error while downloading (the data of) Task "
								+ input_task_ids[i]
								+ ". Please double check whether this is a legal task id. Otherwise some input data might be missing. ",
							"Task download error",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(
				this,
				"Please insert a comma seperated list of task_id's. These are all numbers. ",
				"Wrong input", JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException npe) {
			// catch quietly. User probably pressed cancel.
		}
	}

	public void actionPerformedTaskBasedEdit(ActionEvent e) {
		System.out.println("TODO, function not yet implemented.");
	}
	
	private DefaultListModel<Task> getTasksControlled( Experiment exp ) {
		return ((TaskBasedExperiment) exp).getTasks();
	}
}
