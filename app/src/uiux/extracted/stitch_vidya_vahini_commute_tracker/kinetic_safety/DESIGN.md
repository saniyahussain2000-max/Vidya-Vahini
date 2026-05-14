---
name: Kinetic Safety
colors:
  surface: '#faf9fd'
  surface-dim: '#dbd9dd'
  surface-bright: '#faf9fd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f7'
  surface-container: '#efedf1'
  surface-container-high: '#e9e7eb'
  surface-container-highest: '#e3e2e6'
  on-surface: '#1a1b1e'
  on-surface-variant: '#414754'
  inverse-surface: '#2f3033'
  inverse-on-surface: '#f1f0f4'
  outline: '#727785'
  outline-variant: '#c1c6d6'
  surface-tint: '#005bc0'
  primary: '#005bbf'
  on-primary: '#ffffff'
  primary-container: '#1a73e8'
  on-primary-container: '#ffffff'
  inverse-primary: '#adc7ff'
  secondary: '#795900'
  on-secondary: '#ffffff'
  secondary-container: '#febf0d'
  on-secondary-container: '#6d5000'
  tertiary: '#006d2b'
  on-tertiary: '#ffffff'
  tertiary-container: '#24883f'
  on-tertiary-container: '#000601'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc7ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#ffdfa0'
  secondary-fixed-dim: '#fbbc05'
  on-secondary-fixed: '#261a00'
  on-secondary-fixed-variant: '#5c4300'
  tertiary-fixed: '#96f8a1'
  tertiary-fixed-dim: '#7adb87'
  on-tertiary-fixed: '#002108'
  on-tertiary-fixed-variant: '#00531f'
  background: '#faf9fd'
  on-background: '#1a1b1e'
  surface-variant: '#e3e2e6'
typography:
  headline-lg:
    fontFamily: Public Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
  headline-md:
    fontFamily: Public Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  body-lg:
    fontFamily: Public Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-lg:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 20px
    letterSpacing: 0.5px
  label-md:
    fontFamily: Public Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.3px
  status-display:
    fontFamily: Public Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 8px
  margin-main: 20px
  gutter: 12px
  touch-target-min: 48px
  touch-target-lg: 56px
---

## Brand & Style

The brand personality is rooted in dependability, precision, and public service. This design system prioritizes the mental peace of parents and the efficiency of transport coordinators. The emotional response is one of "calm assurance"—knowing exactly where a vehicle is without having to fight the interface. 

The aesthetic follows a **High-Contrast / Modern** approach. It avoids heavy imagery and complex gradients to ensure the interface remains legible under direct sunlight and performs flawlessly on entry-level hardware. The style is utilitarian; every element exists to convey information or facilitate action. Visual hierarchy is achieved through scale and color blocking rather than decorative depth, ensuring the UI feels lightning-fast even on intermittent 2G/3G connections.

## Colors

The palette is anchored by "Trust Blue" (#1A73E8) for primary actions and "Alert Yellow" (#FBBC04) for critical status updates and tracking indicators. This high-contrast pairing ensures that the most important information—the bus's status—is immediately visible. 

A "Safety Green" (#188038) is utilized specifically for "Completed" or "Safe Arrival" states. The neutral palette leans toward deep charcoals and off-whites to reduce eye strain while maintaining a high contrast ratio for accessibility. Success, warning, and error states must always be accompanied by icons to ensure information is accessible to users with color vision deficiencies.

## Typography

This design system utilizes **Public Sans** across all levels. Chosen for its institutional clarity and exceptional legibility on small screens, it provides a neutral yet authoritative tone. 

Typography is used as a primary tool for hierarchy. Large "Status-Display" sizes are reserved for real-time data like "ETA" or "Bus Number." Body text is slightly oversized (16px–18px) to accommodate use-cases where the user might be walking or in a moving vehicle. Labels use all-caps and increased letter spacing to distinguish metadata from interactive content.

## Layout & Spacing

The layout utilizes a **Fluid Grid** model with a generous 20px outer margin to prevent accidental edge-taps and ensure content isn't obscured by device cases. The system is built on an 8px square baseline grid.

Vertical rhythm is prioritized to allow for easy one-handed scrolling. Touch targets for all interactive elements, including checkboxes and navigation links, must be at least 48px in height, with primary action buttons utilizing a 56px height for maximum ease of use. White space is used functionally to group related information, such as student details and their specific route information.

## Elevation & Depth

To maximize performance on low-end devices, this design system avoids complex shadows and blurs. Hierarchy is instead communicated through **Tonal Layers** and **Bold Outlines**. 

Backgrounds are kept at a neutral off-white. Cards and containers use a white surface with a subtle 1px border (#DADCE0) to define boundaries. When an element is "active" or "in-focus," the border weight increases or shifts to the Primary Blue. This flat-depth approach ensures the GPU is not taxed with rendering soft-shadow calculations, maintaining high frame rates during real-time map tracking.

## Shapes

The design system uses a **Soft** (4px) corner radius for most UI elements. This subtle rounding provides a modern touch without sacrificing the "serious" and "structured" feel of a safety application. 

Buttons and input fields follow this 4px standard, while status chips and notification badges may use a "Pill" shape (fully rounded) to differentiate them from actionable components. This distinction helps users quickly separate static information from interactive buttons.

## Components

### Buttons
Primary buttons are solid Trust Blue with white text, utilizing the 56px height. Secondary buttons use a 1px Blue outline. There are no "ghost" buttons; every action must have a clear, high-contrast boundary.

### Status Indicators
Status indicators are high-visibility chips. A "Live" bus status uses the Alert Yellow background with black text for maximum contrast. "Safe Arrival" uses a solid Green block. These indicators should often be paired with a simple icon (e.g., a bus or a checkmark).

### Input Fields
Fields use a 1px neutral border that turns 2px Trust Blue on focus. Labels must remain visible above the field at all times (no disappearing placeholders) to assist users who may have cognitive load or are in a hurry.

### Tracking Cards
The core component of the app. These cards feature the student's name in Headline-MD, the bus number in a prominent Status-Display style, and a vertical timeline representing the route stops. The timeline uses thick 4px lines to ensure visibility.

### Lists
Lists use generous vertical padding (16px) between items. Each list item should have a clear trailing icon (usually a chevron) to indicate drill-down capability, supporting the system's "functional and accessible" mandate.