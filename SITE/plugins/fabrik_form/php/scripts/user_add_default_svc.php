<?php

/**
* Form PHP plugin helper that is used to optionally create a default
* service record when an admin type adds a NEW person to the db.
* @package Joomla
* @subpackage Fabrik
* @author Bruce Schurmann
* @copyright (C) Bruce Schurmann
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL
*/

/// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die();

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// NB. This PHP Form plugin helper must be set to 'onBeforeCalculations'.
// This means the usre has been entered in the DB and the PersonID is known.
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


// Add the default service record.

if ($defaultjobid && $defaultorgid) {
    if (array_key_exists('PersonID',$formModel->_formData) && ($formModel->_formData['PersonID'] > 0)) {
        $pid = $formModel->_formData['PersonID'];
	$dbo = &JFactory::getDBO();
	$dbo->setQuery("INSERT INTO `".$serviceTable."` SET `personid`=".$pid.",`orgid`=".$defaultorgid.",`jobid`=".$defaultjobid.",`active`='Yes'");
	$dbo->query();
    }
}
	

?>