# Changelog 
see [keepchangelog](https://keepachangelog.com/de/1.1.0/)

## [0.1.0] - 2024-11-19

### Initial version for github

## [0.2.0] - 2024-12-15

### Added

- added method for scanning from a reader

### Changed

- Switch license to apache 2.0
- changed logic for TokenReader. It has a fixed sized and tries to read as much tokens as possible
  before resizing the buffer or removing the accepted buffer

## [0.2.1] - 2024-12-15

### Fixed

- move Readme_tmp.md back to Readme.md