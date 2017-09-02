<?php
    
	include('config.php');
	$json = json_decode(file_get_contents('php://input'),true);
 
    $name = $json["name"]; //within square bracket should be same as Utils.imageName & Utils.imageList
    $imageList = $json["imageList"];
    $i = 0;
	$yy=false;
 
    $response = array();
       $man=array();
    if (isset($imageList)) {
    	if (is_array($imageList)) {
    		foreach($imageList as $image) {
	     		$decodedImage = base64_decode("$image");
	     		$return = file_put_contents("uploads/".$name."_".$i.".JPG", $decodedImage);
				
				$man1[$i]="uploads/".$name."_".$i.".JPG";
				
	     		if($return !== false){
			 
			  $response['success'] = 1;
			  $response['message'] = "Image Uploaded Successfully";
			  $yy=true;
			 
			}else{
			  $response['success'] = 0;
			  $response['message'] = "Image Uploaded Failed";
			}
			$i++;
	       }
    	}
    } else{
    	$response['success'] = 0;
        $response['message'] = "List is empty.";
    }
	if($yy==true)
	{
	$man=json_encode($man1);
	mysql_query("insert into `claim`(`bill_image`)values('$man')");
	}
    echo json_encode($response);
?>
