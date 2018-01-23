// Copyright 2013 The Servo Project Developers. See the COPYRIGHT
// file at the top-level directory of this distribution.
//
// Licensed under the Apache License, Version 2.0 <LICENSE-APACHE or
// http://www.apache.org/licenses/LICENSE-2.0> or the MIT license
// <LICENSE-MIT or http://opensource.org/licenses/MIT>, at your
// option. This file may not be copied, modified, or distributed
// except according to those terms.

use base::CGFloat;
use core_foundation::base::TCFType;
use core_foundation::dictionary::CFDictionary;

pub const CG_ZERO_POINT: CGPoint = CGPoint {
    x: 0.0,
    y: 0.0,
};

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub struct CGSize {
    pub width: CGFloat,
    pub height: CGFloat,
}

impl CGSize {
    #[inline]
    pub fn new(width: CGFloat, height: CGFloat) -> CGSize {
        CGSize {
            width: width,
            height: height,
        }
    }
}

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub struct CGPoint {
    pub x: CGFloat,
    pub y: CGFloat,
}

impl CGPoint {
    #[inline]
    pub fn new(x: CGFloat, y: CGFloat) -> CGPoint {
        CGPoint {
            x: x,
            y: y,
        }
    }
}

#[repr(C)]
#[derive(Clone, Copy, Debug)]
pub struct CGRect {
    pub origin: CGPoint,
    pub size: CGSize
}

impl CGRect {
    #[inline]
    pub fn new(origin: &CGPoint, size: &CGSize) -> CGRect {
        CGRect {
            origin: *origin,
            size: *size,
        }
    }

    #[inline]
    pub fn inset(&self, size: &CGSize) -> CGRect {
        unsafe {
            ffi::CGRectInset(*self, size.width, size.height)
        }
    }

    #[inline]
    pub fn from_dict_representation(dict: &CFDictionary) -> Option<CGRect> {
        let mut rect = CGRect::new(&CGPoint::new(0., 0.), &CGSize::new(0., 0.));
        let result = unsafe {
            ffi::CGRectMakeWithDictionaryRepresentation(dict.as_concrete_TypeRef(), &mut rect)
        };
        if result == 0 {
            None
        } else {
            Some(rect)
        }
    }

    #[inline]
    pub fn is_empty(&self) -> bool {
        unsafe {
            // I use one, as it seems that `YES` is not available from this crate.
            ffi::CGRectIsEmpty(*self) == 1
        }
    }

    #[inline]
    pub fn is_intersects(&self, other: &CGRect) -> bool {
        unsafe {
            // I use one, as it seems that `YES` is not available from this crate.
            ffi::CGRectIntersectsRect(*self, *other) == 1
        }
    }
}

mod ffi {
    use base::{CGFloat, boolean_t};
    use geometry::CGRect;
    use core_foundation::dictionary::CFDictionaryRef;

    #[link(name = "ApplicationServices", kind = "framework")]
    extern {
        pub fn CGRectInset(rect: CGRect, dx: CGFloat, dy: CGFloat) -> CGRect;
        pub fn CGRectMakeWithDictionaryRepresentation(dict: CFDictionaryRef,
                                                      rect: *mut CGRect) -> boolean_t;
        pub fn CGRectIsEmpty(rect: CGRect) -> boolean_t;
        pub fn CGRectIntersectsRect(rect1: CGRect, rect2: CGRect) -> boolean_t;
    }
}
