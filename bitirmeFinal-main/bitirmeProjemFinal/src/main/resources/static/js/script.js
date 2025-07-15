$(function(){

// User Register validation

	var $userRegister=$("#userRegister");

	$userRegister.validate({
		
		rules:{
			name:{
				required:true,
				lettersonly:true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12
			},
			password: {
				required: true,
				space: true
			},
			confirmpassword: {
				required: true,
				space: true,
				equalTo: '#pass'
			},
			address: {
				required: true,
				all: true
			},
			city: {
				required: true,
				space: true

			},
			state: {
				required: true,

			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true

			}, img: {
				required: true,
			}
			
		},
		messages:{
			name:{
				required:'İsim Gerekli',
				lettersonly:'Geçersiz İsim'
			},
			email: {
				required: 'email gerekli',
				space: 'boşluklara izin verilmez',
				email: 'geçersiz email'
			},
			mobileNumber: {
				required: 'telefon numarası gerekli',
				space: 'boşluklara izin verilmez',
				numericOnly: 'geçersiz telefon numarası',
				minlength: 'en az 10 haneli olmalı',
				maxlength: 'en fazla 12 haneli olmalı'
			},
			password: {
				required: 'şifre gerekli',
				space: 'boşluklara izin verilmez'
			},
			confirmpassword: {
				required: 'şifreyi onaylamak gerekli',
				space: 'boşluklara izin verilmez',
				equalTo: 'şifreler uyuşmuyor'
			},
			address: {
				required: 'adres gerekli',
				all: 'geçersiz adres'
			},

			city: {
				required: 'şehir gerekli',
				space: 'boşluklara izin verilmez'

			},
			state: {
				required: 'ilçe gerekli',
				space: 'boşluklara izin verilmez'

			},
			pincode: {
				required: 'posta kodu gerekli',
				space: 'boşluklara izin verilmez',
				numericOnly: 'geçersiz posta kodu'

			},
			img: {
				required: 'Resim Gerekli',
			}
		}
	})
	
	
// Orders Validation

var $orders=$("#orders");

$orders.validate({
		rules:{
			firstName:{
				required:true,
				lettersonly:true
			},
			lastName:{
				required:true,
				lettersonly:true
			}
			,
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNo: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 10,
				maxlength: 12

			},
			address: {
				required: true,
				all: true

			},

			city: {
				required: true,
				space: true

			},
			state: {
				required: true,


			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true

			},
			paymentType:{
			required: true
			}
		},
	messages:{
		firstName:{
			required:'isim gerekli',
			lettersonly:'geçersiz isim'
		},
		lastName:{
			required:'soyisim gerekli',
			lettersonly:'geçersiz isim'
		},
		email: {
			required: 'email gerekli',
			space: 'boşluklara izin verilmez',
			email: 'geçersiz email'
		},
		mobileNo: {
			required: 'telefon numarası gerekli',
			space: 'boşluklara izin verilmez',
			numericOnly: 'geçersiz telefon numarası',
			minlength: 'en az 10 haneli olmalı',
			maxlength: 'en fazla 12 haneli olmalı'
		},
		address: {
			required: 'adres gerekli',
			all: 'geçersiz adres'
		},

		city: {
			required: 'şehir gerekli',
			space: 'boşluklara izin verilmez'
		},
		state: {
			required: 'il gerekli',
			space: 'boşluklara izin verilmez'
		},
		pincode: {
			required: 'posta kodu gerekli',
			space: 'boşluklara izin verilmez',
			numericOnly: 'geçersiz posta kodu'
		},
		paymentType:{
			required: 'ödeme türü seçilmeli'
		}
	}
})

// Reset Password Validation

var $resetPassword=$("#resetPassword");

$resetPassword.validate({
		
		rules:{
			password: {
				required: true,
				space: true

			},
			confirmPassword: {
				required: true,
				space: true,
				equalTo: '#pass'

			}
		},
		messages:{
			password: {
				required: 'şifre gerekli',
				space: 'boşluklara izin verilmez'
			},
			confirmPassword: {
				required: 'şifreyi onaylamak gerekli',
				space: 'boşluklara izin verilmez',
				equalTo: 'şifreler uyuşmuyor'
			}
		}	
})
	
})


jQuery.validator.addMethod('lettersonly', function(value, element) {
	return /^[^-\s][a-zA-ZçğöşüÇĞÖŞİı0-9_.,;:!?'\s-]+$/.test(value);
	});
	
		jQuery.validator.addMethod('space', function(value, element) {
		return /^[^-\s]+$/.test(value);
	});

	jQuery.validator.addMethod('all', function(value, element) {
		return /^[^-\s][a-zA-ZçğöşüÇĞÖŞİı0-9_.,;:!?'\s-]+$/.test(value);
	});


	jQuery.validator.addMethod('numericOnly', function(value, element) {
		return /^[0-9]+$/.test(value);
	});
