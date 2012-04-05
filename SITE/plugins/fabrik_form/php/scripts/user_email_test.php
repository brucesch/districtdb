<?php

/**
* Form PHP plugin helper that is used to reconcile a new user "registration" when
* that user has previously been defined in the DistrictDB Persons table but does
* not yet have a Joomla Users presence.
* @package Joomla
* @subpackage Fabrik
* @author Bruce Schurmann
* @copyright (C) Bruce Schurmann
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL
*/

/// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die();

// Test is: (email in Persons) && (email not in JUser)

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// NB. This PHP Form plugin helper must be run before the JUser plugin to work correctly!!
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// -----------------  Edit these config variables to match your setup.

// This is the name of the db table that is for people.
$tableName = 'districtdb_persons';
// This is the name of the primary key field in the above db table.
$personidfld = 'PersonID';
// This is the name of the 'email' field in the above db table.
$emailfld  = 'email';                          
// This is the name of the 'userid' field in the above db table.
$useridfld = 'userid';

// -----------------  End of config.

// DO NOT EDIT BELOW THIS LINE.

// First the Persons test
$dbo = &JFactory::getDBO();
$emailval  = $formModel->_formData[$tableName.'___'.$emailfld];
$whereclause = "WHERE LOWER(`".preg_replace('/^('.$tableName.'___)/', '', $tableName.'___'.$emailfld)."`)=LOWER('".$emailval."')";
$dbo->setQuery("SELECT `".$personidfld."` FROM `".$tableName."` ".$whereclause);
$foundperson = $dbo->loadResult();

// Now the JUser test
$whereclause = "WHERE LOWER(`email`)=LOWER('".$emailval."')";
$dbo->setQuery("SELECT `id` FROM `#__users` ".$whereclause);
$founduser = $dbo->loadResult();


if ($foundperson && !$founduser) {
    // already should be 0 - but let's be sure
    // Also note that the JUser plugin (which runs after this plugin) is going to udpate this field.
    $formModel->updateFormData($tableName.'___'.$useridfld.'_raw', 0, true);
    // This is the juicey bit - setting the rowid datum morphes the "register"
    // form from an "add" form to an "edit" form.
    $formModel->_rowId = $foundperson;
    // It appears that you need to do a bunch of work to get this form slammed over to an "edit" form
    $formModel->updateFormData($tableName.'___'.$personidfld, $foundperson, true);
    $formModel->updateFormData($tableName.'___'.$personidfld.'_raw', $foundperson, true);
    JRequest::setVar('rowid', $foundperson);
    $_POST['rowid'] = $foundperson;
    $_REQUEST['rowid'] = $foundperson;    
}
?>
