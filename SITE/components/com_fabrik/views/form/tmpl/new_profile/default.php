<!-- The default template includes the following folder and files:

images - this is the folder for the form template's images
- add.png
- alert.png
- delete.png
default.php - this file controls the layout of the form
default_group.php - this file controls the layout of the individual form groups
default_relateddata.php - this file controls the layout of the forms related data
template_css.php - this file controls the styling of the form

CSS classes and id's included in this file are:

componentheading - used if you choose to display the page title
<h1> - used if you choose to show the form label
fabrikMainError - 
fabrikError - 
fabrikGroup - 
groupintro - 
fabrikSubGroup - 
fabrikSubGroupElements - 
fabrikGroupRepeater - 
addGroup - 
deleteGroup - 
fabrikTip - 
fabrikActions - 

Other form elements that can be styled here are:

legend
fieldset

To learn about all the different elements in a basic form see http://www.w3schools.com/tags/tag_legend.asp.  

-->

<!--If you have set to show the page title in the forms layout parameters, then the page title will show-->
<?php if ($this->params->get('show_page_title', 1)) { ?>
	<div class="componentheading<?php echo $this->params->get('pageclass_sfx')?>"><?php echo $this->escape($this->params->get('page_title')); ?></div>
<?php } ?>
<?php $form = $this->form;
echo $form->startTag;
if ($this->params->get('show-title', 1)) {?>

<!--This will show the forms label-->
<h1><?php echo $form->label;?></h1>

<!--This area will show the form's intro as well as any errors-->
<?php }
echo $form->intro;
echo $this->plugintop;
$active = ($form->error != '') ? '' : ' fabrikHide';
echo "<div class=\"fabrikMainError fabrikError$active\">";
echo FabrikHelperHTML::image('alert.png', 'form', $this->tmpl);
echo "$form->error</div>";?>
	<?php
	if ($this->showEmail) {
		echo $this->emailLink;
	}
	if ($this->showPDF) {
		echo $this->pdfLink;
	}
	if ($this->showPrint) {
		echo $this->printLink;
	}
	echo $this->loadTemplate('relateddata');
	foreach ($this->groups as $group) {
		?>
		
<!-- This is where the fieldset is set up -->
		<fieldset class="fabrikGroup" id="group<?php echo $group->id;?>" style="<?php echo $group->css;?>">
		<?php if (trim($group->title) !== '') {?>
		
<!-- This is where the legend is set up -->
			<legend><span><?php echo $group->title;?></span></legend>
		<?php }?>
		
<!-- This is where the group intro is shown -->
		<?php if ($group->intro !== '') {?>
		<div class="groupintro"><?php echo $group->intro ?></div>
		<?php }?>
		
		<?php if ($group->canRepeat) {
			foreach ($group->subgroups as $subgroup) {
			?>
				<div class="fabrikSubGroup">
					<div class="fabrikSubGroupElements">
						<?php
						$this->elements = $subgroup;
						echo $this->loadTemplate('group');
						?>
					</div>
					<?php if ($group->editable) { ?>
						<div class="fabrikGroupRepeater">
							<a class="addGroup" href="#">
								<?php echo FabrikHelperHTML::image('add.png', 'form', $this->tmpl, array('class' => 'fabrikTip','opts' => "{notice:true}", 'title' => JText::_('COM_FABRIK_ADD_GROUP')));?>
							</a>
							<a class="deleteGroup" href="#">
								<?php echo FabrikHelperHTML::image('del.png', 'form', $this->tmpl, array('class' => 'fabrikTip','opts' => "{notice:true}", 'title' => JText::_('COM_FABRIK_DELETE_GROUP')));?>
							</a>
						</div>
					<?php } ?>
				</div>
				<?php
			}
		} else {
// bruce
// This is where I customize the form to move the 'captcha' element to the very end of the form.
// Look for 'captcha' in elements. If there then copy it and remove it.
// Process like usual then append the 'captcha' at the end.
			$this->elements = $group->elements;
                        $captchaelemobj = new stdClass();
                        if (array_key_exists('captcha', $this->elements)) {
                            $captchaelemobj = $this->elements['captcha'];
                            unset($this->elements['captcha']);
                        }
			echo $this->loadTemplate('group');
		}?>
	</fieldset>
<?php
	}

// bruce
// This is where we poke in the 'captcha' from above.
        if (property_exists($captchaelemobj, 'plugin')) {

?>
		<fieldset class="fabrikGroup" id="groupCaptcha" style="<?php echo $group->css;?>">
<?php

            $this->elements = array();
            $this->elements['captcha'] = $captchaelemobj;
	    echo $this->loadTemplate('group');
?>
	</fieldset>
<?php
	}


	echo $this->hiddenFields;
	?>
	<?php echo $this->pluginbottom; ?>
	
<!-- This is where the buttons at the bottom of the form are set up -->
	<div class="fabrikActions"><?php echo $form->resetButton;?> <?php echo $form->submitButton;?>
	<?php echo $form->nextButton?> <?php echo $form->prevButton?>
	 <?php echo $form->applyButton;?>
	<?php echo $form->copyButton  . " " . $form->gobackButton . ' ' . $form->deleteButton . ' ' . $this->message ?>
	</div>

<?php
echo $form->endTag;
echo FabrikHelperHTML::keepalive();?>
