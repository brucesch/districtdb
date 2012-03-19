<?php

/**
* Form PHP plugin helper that is used to take the 'name' field (full name)
* from the form and crack it into 'firstname' and 'lastname' behind the 
* scenes.
* @package Joomla
* @subpackage Fabrik
* @author Bruce Schurmann
* @copyright (C) Bruce Schurmann
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL
*/

/// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die();

// The FullContact (fullcontact.com) API
require_once(COM_FABRIK_FRONTEND.DS.'libs'.DS.'fullcontact'.DS.'FullContact.php');

// -----------------  Edit these config variables to match your setup.

// This is the name of the db table that is for people.
$tableName = 'districtdb_persons';
// This is the name of the name field in the above db table.
$namefld = 'name';
// This is the name of the firstname field in the above db table.
$firstnamefld = 'firstname';
// This is the name of the lastname field in the above db table.
$lastnamefld = 'lastname';
// FullContact API key
$fcapikey = '192d2a6b8f887a0b';

// -----------------  End of config.

// DO NOT EDIT BELOW THIS LINE.


// Grab the fullname.
$nameval = trim($formModel->_formData[$tableName.'___'.$namefld]);

// Simplest test of all, is there just two name elements and no ','?
$names = preg_split("/\s+/", $nameval);
if ((strpos($nameval, ',') === false) && (count($names) == 2)) {
  // Got the easy case
  $formModel->updateFormData($tableName.'___'.$firstnamefld, $names[0], true);
  $formModel->updateFormData($tableName.'___'.$lastnamefld, $names[1], true);
} else {
  // We have something funky - ask the big boys
  $fullcontact = new FullContactAPI($fcapikey);
  $nameinfo = $fullcontact->doNameLookup($nameval);
  if (is_array($nameinfo) && ($nameinfo['status'] == 200)) {
    // Jam the first name and any middle names together
    $frstname = $nameinfo['nameDetails']['givenName'];
    foreach ($nameinfo['nameDetails']['middleNames'] as $mdlname) {
      $frstname .= " " . $mdlname;
    }
    $formModel->updateFormData($tableName.'___'.$firstnamefld, $frstname, true);
    $formModel->updateFormData($tableName.'___'.$lastnamefld, $nameinfo['nameDetails']['familyName'], true);
  } else {
    // Yikes - cannot figure out name - smash the whole name into the lastname field
    $formModel->updateFormData($tableName.'___'.$firstnamefld, '', true);
    $formModel->updateFormData($tableName.'___'.$lastnamefld, $nameval, true);
  }
}


?>
