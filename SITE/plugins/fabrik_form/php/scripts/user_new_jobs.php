<?php

/**
* Form PHP plugin helper that is used to help with the assignment
* of service records for users that are creating their first profile (New Profile).
* @package Joomla
* @subpackage Fabrik
* @author Bruce Schurmann
* @copyright (C) Bruce Schurmann
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL
*/

/// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die();

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// NB. This PHP Form plugin helper must be run after the JUser plugin to work correctly!!
//     (If the JUser plugin is used on this form.)
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


jimport('joomla.application.component.helper');

// -----------------  Edit these config variables to match your setup.

// This is the name of the DistrictDB service table
$serviceTable = 'districtdb_service';

// You can define a default service that is assigned to all new users.
// e.g. "Friend of District ..."
// If there is no default service then set these values to 0 (zero).
$defaultjobid = 134;  // Friend of District - Job
$defaultorgid = 71;   // Bee Cave District - Organization

// -----------------  End of config.

// DO NOT EDIT BELOW THIS LINE.


// Grab any service records that this user may have.
// 'user_email_test.php' may have figured out for us that this "new" form
// is really for an existing user. Sniff rowid to find out.
$persexistingsvc = array();
$personid = $formModel->getRowId();
if (isset($personid) && ($personid > 0)) {
    $whereclause = " WHERE `personid`=".$personid;
    $dbo = &JFactory::getDBO();
    $dbo->setQuery("SELECT `ServiceID`,`personid`,`orgid`,`jobid`,`active`,`activedate`,`inactivedate` FROM `".$serviceTable."` ".$whereclause);
    $persexistingsvc = $dbo->loadObjectList();
}

// Extract the service entries (if any) from the form
$purgeform = array();
$joinid = 0;
$formsvc = array();
if (array_key_exists('join',$formModel->_formData) && (count($formModel->_formData['join']) == 1)) {
    reset($formModel->_formData['join']);
    $joinid = key($formModel->_formData['join']);
    foreach ($formModel->_formData['join'][$joinid] as $elem => $val) {
        if ($elem == 'districtdb_service___orgid_raw' || $elem == 'districtdb_service___jobid_raw') {
            foreach ($val as $entry => $eval) {
                if (is_array($eval)) {
                    if (isset($eval[0]) && ($eval[0] > 0)) {
                        $formsvc[$entry][$elem] = $eval[0];
                    } else {
                        $purgeform[$entry] = 1;
                    }
                } else {
                    if (isset($eval) && ($eval > 0)) {
                        $formsvc[$entry][$elem] = $eval;
                    } else {
                        $purgeform[$entry] = 1;
                    }
                }
            }
        }
    }
}
            

// Based on walk throught the form there may be entries that need to be pruned to avoid
// bad db entries.
foreach ($purgeform as $entry => $val) {
    purgeFormEntry($joinid, $entry, $formModel->_formData);
    if (array_key_exists($entry,$formsvc)) {
        unset($formsvc[$entry]);
    }
}


// If there is a default service then do some checking.
if ($defaultjobid && $defaultorgid) {
    // We add the default only if the user has or will have no active service records
    if (empty($formsvc) && empty($persexistingsvc)) {
        addFormEntry($defaultjobid, $defaultorgid, $joinid, $formModel->_formData);
        addFormSvcEntry($defaultjobid, $defaultorgid, $formsvc);
    } elseif (empty($formsvc) && wasInActive($defaultjobid, $defaultorgid, $persexistingsvc)) {
        // Nothing to do. If got here then wasInActive() found an inactive default svc record
        // in the DB array where all the other service records were also inactive and 
        // switched it to 'Yes'. Down below this DB record gets added to the form.
    } elseif (empty($formsvc) && noActive($persexistingsvc)) {
        addFormEntry($defaultjobid, $defaultorgid, $joinid, $formModel->_formData);
        addFormSvcEntry($defaultjobid, $defaultorgid, $formsvc);
    }
}


// Check for pre-existing entries in the db to avoid dupes
$purgeform = array();
if ($persexistingsvc) {
    foreach ($formsvc as $entry => $val) {
        if ($srec = inDB($val, $persexistingsvc)) {
            if ($srec->active == 'No') {
                // Make this entry in the DB active since the user has it in the form
                // Note that the svc array is an array of objects and an object is returned
                $srec->active = 'Yes';
            }
            // Take out of form since it is in the DB already.
            // Note that this is funky because we will add this Db entry back to the form below
            // since otherwise the DB entry will get deleted.
            $purgeform[$entry] = 1;
        }
    }
}
// Clean up form
foreach ($purgeform as $entry => $val) {
    purgeFormEntry($joinid, $entry, $formModel->_formData);
}

// Finally - the form needs to have the records that are in the DB, otherwise they
// will get deleted.
if ($persexistingsvc) {
    addDBtoForm($persexistingsvc, $joinid, $formModel->_formData);
}


// -----------------------  helper functions

// purgeFormEntry
// Prune empty elements from the form.
function purgeFormEntry($joinid, $entry, &$formarray) {
    foreach ($formarray['join'][$joinid] as $elem => $val) {
      unset($formarray['join'][$joinid][$elem][$entry]);
      $formarray['join'][$joinid][$elem] = array_merge($formarray['join'][$joinid][$elem]);
    }
    // This is a bit funky but . . . 
    end($formarray['fabrik_repeat_group']);
    $formarray['fabrik_repeat_group'][key($formarray['fabrik_repeat_group'])]--;
    $repeatTotals = JRequest::getVar('fabrik_repeat_group', array(0), 'post', 'array');
    end($repeatTotals);
    $repeatTotals[key($repeatTotals)]--;
    JRequest::setVar('fabrik_repeat_group', $repeatTotals, 'post', true);
}

// noActive
// Scan array and return true if there are no active service records
function noActive($srvarray) {
    $noactive = true;
    foreach ($srvarray as $idx => $val) {
        if ($srvarray[$idx]->active == 'Yes') {
            $noactive = false;
        }
    }
    return $noactive;
}

// wasInActive($defaultjobid, $defaultorgid, $persexistingsvc)
// Look through the  DB svc record array for a match of the default
// If default record is found AND it is active='No' AND
// any other non-default records are all active='No' THEN
// tweak it to active='Yes' and return TRUE.
function wasInActive($defaultjobid, $defaultorgid, &$srvarray) {
    $switched = false;
    $allinactive = true;
    foreach ($srvarray as $idx => $val) {
        if ($srvarray[$idx]->active == 'Yes') {
          $allinactive = false;
        }
    }
    // If all the records are inactive then sniff to see if one of them is the default. 
    // If found then switch it.
    if ($allinactive) {
        foreach ($srvarray as $idx => $val) {
            if (($srvarray[$idx]->jobid == $defaultjobid) && ($srvarray[$idx]->orgid == $defaultorgid) && ($srvarray[$idx]->active == 'No')) {
                $srvarray[$idx]->active = 'Yes';
                $switched = true;
            }
        }
    }
    return ($switched && $allinactive);
}

// addFormEntry
// Add an entry (typically the default) to the form so that it will added to db when processed.
function addFormEntry($defaultjobid, $defaultorgid, $joinid, &$formarray) {
    foreach ($formarray['join'][$joinid] as $elem => $val) {
        if (($elem == 'districtdb_service___orgid') || ($elem == 'districtdb_service___orgid_raw')) {
            $formarray['join'][$joinid][$elem][] = array($defaultorgid);
        } elseif (($elem == 'districtdb_service___jobid') || ($elem == 'districtdb_service___jobid_raw')) {
            $formarray['join'][$joinid][$elem][] = array($defaultjobid);
        } elseif (($elem == 'districtdb_service___active') || ($elem == 'districtdb_service___active_raw')) {
            $formarray['join'][$joinid][$elem][] = 'Yes';
        } else {
            $formarray['join'][$joinid][$elem][] = NULL;
        }
    }
    // This is a bit funky but . . . 
    end($formarray['fabrik_repeat_group']);
    $formarray['fabrik_repeat_group'][key($formarray['fabrik_repeat_group'])]++;
    $repeatTotals = JRequest::getVar('fabrik_repeat_group', array(0), 'post', 'array');
    end($repeatTotals);
    $repeatTotals[key($repeatTotals)]++;
    JRequest::setVar('fabrik_repeat_group', $repeatTotals, 'post', true);
}

// addDBtoForm
// Add all entries that are in the DB to the form so that they will not be deleted.
function addDBtoForm($persexistingsvc, $joinid, &$formarray) {
    foreach ($persexistingsvc as $entry => $val) {
        foreach ($formarray['join'][$joinid] as $elem => $eval) {
            $tstr = preg_replace('/^.*___/', '', $elem);
            $elemarr = split('_', $tstr);
            if (($elemarr[0] == 'orgid') || ($elemarr[0] == 'jobid')) {
                $formarray['join'][$joinid][$elem][] = array($val->$elemarr[0]);
            } else {
                $formarray['join'][$joinid][$elem][] = $val->$elemarr[0];
            }
        }
        // This is a bit funky but . . . 
        end($formarray['fabrik_repeat_group']);
        $formarray['fabrik_repeat_group'][key($formarray['fabrik_repeat_group'])]++;
        $repeatTotals = JRequest::getVar('fabrik_repeat_group', array(0), 'post', 'array');
        end($repeatTotals);
        $repeatTotals[key($repeatTotals)]++;
        JRequest::setVar('fabrik_repeat_group', $repeatTotals, 'post', true);
    }
}


// addFormSvcEntry($defaultjobid, $defaultorgid, $formsvc);
// Add an entry to the service form array
function addFormSvcEntry($defaultjobid, $defaultorgid, &$formsvc) {
    $temparr = array();
    $temparr['districtdb_service___jobid_raw'] = $defaultorgid;
    $temparr['districtdb_service___orgid_raw'] = $defaultjobid;
    $formsvc[] = $temparr;
}


// inDB($val, $persexistingsvc)
// If this $formsvc entry is in the DB internal array then return it, else NULL
function inDB($fval, $persexistingsvc) {
    $retval = NULL;
    foreach ($persexistingsvc as $entry => $val) {
        if (($fval['districtdb_service___jobid_raw'] == $val->jobid) &&
            ($fval['districtdb_service___orgid_raw'] == $val->orgid)) {
        $retval = $val;
        }
    }
    return $retval;
}

?>