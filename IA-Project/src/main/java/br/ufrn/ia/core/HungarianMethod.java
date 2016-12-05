package br.ufrn.ia.core;

import java.util.Arrays;

public class HungarianMethod {

	public int[] evaluate(int[][] clusterings) {
		clusterings = normalizeMatrix(clusterings);

		minimumRow(clusterings);
		minimumCol(clusterings);

		int[][] marks = new int[clusterings.length][clusterings[0].length];
		for (int i = 0; i < marks.length; i++)
			Arrays.fill(marks[i], 1);

		int count = getMinZerosDraw(clusterings, marks, 0);

		while (count < Math.min(clusterings[0].length, clusterings.length)) {
			smallestEntry(clusterings, marks);
			for (int i = 0; i < marks.length; i++)
				Arrays.fill(marks[i], 1);
			count = getMinZerosDraw(clusterings, marks, 0);
		}

		int[] assignment = new int[clusterings.length];
		assignment(assignment, clusterings, 0);

		return assignment;
	}

	private int assignment(int[] assig, int[][] cost, int position) {
		if (position < assig.length) {
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < cost[0].length; i++) {
				boolean dif = true;
				for (int j = 0; j < position; j++)
					if (assig[j] == i)
						dif = false;

				if (cost[position][i] == 0 && dif) {
					int[] a = assig.clone();
					a[position] = i;
					int value = assignment(a, cost, position + 1);

					if (value < min) {
						min = value;
						for (int j = 0; j < assig.length; j++)
							assig[j] = a[j];
					}
				}
			}
			return min;
		}
		return 0;
	}

	private int getMinZerosDraw(int[][] matrix, int[][] marks, int deep) {
		boolean hasUnmarkedZero = false;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				hasUnmarkedZero = hasUnmarkedZero || (matrix[i][j] == 0 && marks[i][j] == 1);
			}
		}

		if (hasUnmarkedZero) {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					if (matrix[i][j] == 0 && marks[i][j] == 1) {
						int[][] newMarksH = new int[marks.length][marks[0].length];
						copyMatrix(marks, newMarksH);
						int[][] newMarksV = new int[marks.length][marks[0].length];
						copyMatrix(marks, newMarksV);
						for (int k = 0; k < marks[0].length; k++)
							newMarksH[i][k] = 0;
						for (int k = 0; k < marks.length; k++)
							newMarksV[k][j] = 0;

						int deepH = getMinZerosDraw(matrix, newMarksH, deep + 1);
						int deepV = getMinZerosDraw(matrix, newMarksV, deep + 1);
						copyMatrix(deepH < deepV ? newMarksH : newMarksV, marks);
						deep = deepH < deepV ? deepH : deepV;
					}
				}
			}
		}
		return deep;
	}

	private void copyMatrix(int[][] matrix, int[][] destination) {
		for (int i = 0; i < destination.length; i++) {
			for (int j = 0; j < destination[i].length; j++) {
				destination[i][j] = matrix[i][j];
			}
		}
	}

	private void smallestEntry(int[][] matrix, int[][] marks) {
		int value = Integer.MAX_VALUE;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (marks[i][j] == 1 && matrix[i][j] < value) {
					value = matrix[i][j];
				}
			}
		}

		for (int i = 0; i < matrix.length; i++) {
			boolean covered = true;
			for (int j = 0; j < matrix[i].length; j++)
				covered = covered && marks[i][j] == 0;
			if (!covered) {
				for (int j = 0; j < matrix[i].length; j++)
					matrix[i][j] -= value;
			}
		}

		for (int i = 0; i < matrix[0].length; i++) {
			boolean covered = true;
			for (int j = 0; j < matrix.length; j++)
				covered = covered && marks[j][i] == 0;
			if (covered) {
				for (int j = 0; j < matrix.length; j++)
					matrix[j][i] += value;
			}
		}
	}

	private void minimumCol(int[][] matrix) {
		for (int i = 0; i < matrix[0].length; i++) {
			int min = Integer.MAX_VALUE;
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[j][i] < min)
					min = matrix[j][i];
			}
			for (int j = 0; j < matrix.length; j++)
				matrix[j][i] -= min;
		}
	}

	private void minimumRow(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			int min = Integer.MAX_VALUE;
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] < min)
					min = matrix[i][j];
			}
			for (int j = 0; j < matrix[i].length; j++)
				matrix[i][j] -= min;

		}
	}

	private int[][] normalizeMatrix(int[][] matrix) {
		int[][] m = new int[matrix[0].length][matrix[0].length];
		if (matrix.length < matrix[0].length) {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					m[i][j] = matrix[i][j];
				}
			}
		} else {
			m = matrix;
		}
		return m;
	}
}
