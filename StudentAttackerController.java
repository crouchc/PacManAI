package edu.ufl.cise.cs1.controllers;
import game.controllers.AttackerController;
import game.models.*;
import java.awt.*;
import java.util.List;

public final class StudentAttackerController implements AttackerController
{
	public void init(Game game) { }

	public void shutdown(Game game) { }

	public int update(Game game,long timeDue) {
		int action = -1;

		//Declaration of important objects and variables
		List pPill = game.getPowerPillList();
		List pills = game.getPillList();
		List defenders = game.getDefenders();
		Actor attacker = game.getAttacker();
		Attacker att = game.getAttacker();
		Node attNode = att.getLocation();
		Maze maze = game.getCurMaze();
		Node attLoc = att.getLocation();

		Defender closestDef = findDef(defenders, attacker);

		//Checks if there are any power pills left and removes ones that have been eaten from list
		if (!pPill.isEmpty()) {
			for (int i = 0; i < pPill.size(); i++) {
				if (!game.checkPowerPill((Node) pPill.get(i))) {
					pPill.remove(i);
				}
			}

			//Define closest power pill and pill from methods below
			Node closestPpill = findPpill(pPill, attacker);
			Node closestPill = findPill(pills, attacker);

			//First main decision is made based on whether defender is vulnerable or not
			if (closestDef.isVulnerable()) {
				action = att.getNextDir(closestDef.getLocation(), true);
			}

			//If the closest defender is no vulnerable go through these steps
			else if (!closestDef.isVulnerable()) {
				//check if there is a power pill within 40 units of attacker and if no go towards it
				if (attNode.getPathDistance(closestPpill) > 40) {
					action = att.getNextDir(closestPpill, true);
				}
				//If a power pill is close and a defender is not close then eat regular pills til the defender gets close
				else {
					if (attLoc.getPathDistance(closestDef.getLocation()) > 40) {
						if (attLoc.getPathDistance(closestPpill) > 35) {
							action = att.getNextDir(findPpill(pPill, attacker), true);
						} else {

							// This section stops the attacker from accidentally eating power pills when hunting for regular pills
							boolean check = false;
							for (int i = 0; i < pPill.size(); i++) {
								if (attacker.getPathTo(closestPill).contains(pPill.get(i))) {
									check = true;
								}
								else {
									check = false;
								}
							}
							if (!check) {
								action = att.getNextDir(findPill(pills, attacker), true);
							}
							else {
								action = att.getNextDir(closestPpill,false);
							}
						}
					}
					//If the powerpill is closer than the defender then approach it
					else if (attLoc.getPathDistance(closestPpill) < attLoc.getPathDistance(closestDef.getLocation())) {
						action = att.getNextDir(closestPpill, true);
						// Once the attacker is right next to the power pill hold position until the defender gets close
						if (attLoc.getPathDistance(closestPpill) < 5 && attLoc.getPathDistance(closestDef.getLocation()) > 10) {
							action = att.getReverse();
						}
					}
					//If none of the above are true run away from the defender
					else {
						action = att.getNextDir(closestDef.getLocation(), false);
					}
				}
			}
			//If none of the above are true just approach a power pill
			else {
				action = att.getNextDir(closestPpill, true);
			}
		}
		//Once all the power pills have been eaten hunt the regular pills
		else {
			if (!pills.isEmpty()) {
				for (int i = 0; i < pills.size(); i++) {
					if (!game.checkPill((Node) pills.get(i))) {
						pills.remove(i);
					}
				}
				Node closestPill = findPill(pills, attacker);
				for (int i = 0; i < pills.size(); i++) {
					action = att.getNextDir(closestPill, true);
					break;
				}
			}
		}
		return action;
	}
	//Returns the node of the closest power pill
	private Node findPpill(List pPill, Actor attacker) {
		return attacker.getTargetNode(pPill, true);
	}
	//Returns the node of the closest regular pill
	private Node findPill(List pills, Actor attacker) {
		return attacker.getTargetNode(pills, true);
	}
	//returns the Defender object of the closest defender to the attacker
	private Defender findDef(List defenders, Actor attacker) {
		return (Defender) attacker.getTargetActor(defenders, true);
	}
}